package main.services;

import lombok.RequiredArgsConstructor;
import main.api.requests.ApiRequestBody;
import main.api.responses.*;
import main.model.*;
import main.model.enums.Errors;
import main.model.enums.ModerationStatus;
import main.model.enums.Settings;
import main.model.GlobalSettings;
import main.repositories.*;
import main.api.responses.bodies.ErrorsBody;
import main.api.responses.bodies.TagsBody;
import main.services.interfaces.AuthService;
import main.services.interfaces.GeneralService;
import main.services.interfaces.UtilitiesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import jakarta.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GeneralServiceImpl implements GeneralService {

    private final TagRepository tagRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostCommentRepository postCommentRepository;
    private final AuthService authService;
    private final GlobalSettingsRepository globalSettingsRepository;
    private final UtilitiesService utilitiesService;

    @Value("${storage.location}")
    private String location;

    @Value("${comment.min.length}")
    private int commentMinLength;

    @Value("${avatar.name.prefix}")
    private String avatarPrefix;

    @Override
    public ResponseEntity<TagsResponseBody> getTags(String query) {

        byte isActive = utilitiesService.getIsActive();
        ModerationStatus moderationStatus = ModerationStatus.valueOf(utilitiesService.getModerationStatus());
        LocalDateTime time = utilitiesService.getTime();

        int count = postRepository.getPostsCountByActiveAndModStatusAndTime(isActive, moderationStatus, time);
        List<TagsBody> tags = new ArrayList<>();
        if (query == null)
            query = "";

        List<Tag> tagList = tagRepository.findByNameStartingWith(query);
        HashMap<Tag, Double> tagsWeightMap = new HashMap<>();
        for (Tag tag : tagList) {
            int postsCount = postRepository.getTotalPostByTag(isActive, moderationStatus, time, tag.getId());
            double weight = (double) postsCount / (double) count;
            tagsWeightMap.put(tag, weight);
        }
        double maxWeight = 0.0;
        double rate;
        if (!tagsWeightMap.isEmpty())
            maxWeight = tagsWeightMap.values().stream().max(Double::compareTo).get();

        if (maxWeight == 0.0)
            return ResponseEntity.ok(new TagsResponseBody(tags));
        else rate = 1 / maxWeight;

        for(Map.Entry<Tag,Double> entry : tagsWeightMap.entrySet()) {
            if (entry.getValue() != maxWeight) {
                double weight = entry.getValue() * rate;
                Tag tag = entry.getKey();
                tags.add(new TagsBody(tag.getName(), weight));
            }
            else tags.add(new TagsBody(entry.getKey().getName(), 1));
        }
        return ResponseEntity.ok(new TagsResponseBody(tags));
    }

    @Override
    public ResponseEntity<CalendarResponseBody> getCalendar(String year)
    {
        LocalDateTime currentTime = utilitiesService.getTime();
        byte isActive = utilitiesService.getIsActive();
        ModerationStatus moderationStatus = ModerationStatus.valueOf(utilitiesService.getModerationStatus());

        if (year == null
                || !year.matches("[0-9]{4}")
                || Integer.parseInt(year) > currentTime.getYear())
            year = Integer.toString(currentTime.getYear());

        List<Integer> years = postRepository.getYears(isActive, moderationStatus, currentTime);
        TreeMap<String, Long> posts = new TreeMap<>();
        List<Object[]> postsInYear = postRepository.getPostCountInYearGroupByDate(isActive,
                moderationStatus,
                currentTime,
                Integer.parseInt(year));
        postsInYear.forEach(postInYear -> {
            String day = postInYear[1].toString();
            Long count = (Long) postInYear[0];
            posts.put(day,count);
        });
        return ResponseEntity.ok(new CalendarResponseBody(years, posts));
    }

    @Override
    public ResponseEntity<SettingsResponseBody> getSettings()
    {
        boolean multiuserMode = false;
        boolean postPremoderation = false;
        boolean statisticsIsPublic = false;

        List<GlobalSettings> globalSettings = globalSettingsRepository.findAll();

        for (GlobalSettings mySettings : globalSettings)
        {
            boolean value = mySettings.getValue().equals("YES");
            if (mySettings.getCode().equals(Settings.MULTIUSER_MODE.toString()))
                multiuserMode = value;
            if (mySettings.getCode().equals(Settings.POST_PREMODERATION.toString()))
                postPremoderation = value;
            if (mySettings.getCode().equals(Settings.STATISTICS_IS_PUBLIC.toString()))
                statisticsIsPublic = value;
        }
        return ResponseEntity.ok(new SettingsResponseBody(multiuserMode, postPremoderation, statisticsIsPublic));
    }

    @Override
    @Transactional
    public ResponseEntity<SettingsResponseBody> putSettings(boolean multiuserMode,
                                                            boolean postPremoderation, boolean statisticsIsPublic) {
        HashMap<String, Boolean> codes = new HashMap<>();
        codes.put(Settings.MULTIUSER_MODE.toString(), multiuserMode);
        codes.put(Settings.POST_PREMODERATION.toString(), postPremoderation);
        codes.put(Settings.STATISTICS_IS_PUBLIC.toString(), statisticsIsPublic);

        if (authService.isUserAuthorize()) {
            User user = authService.getAuthorizedUser();
            if (user.getIsModerator() == 1) {
                for (Map.Entry<String, Boolean> code : codes.entrySet()) {
                    String value = "NO";
                    boolean isCodeExist = code.getValue();
                    if (isCodeExist) {
                        value = "YES";
                    }
                    GlobalSettings settings = globalSettingsRepository.findByCode(code.getKey());
                    settings.setValue(value);
                    globalSettingsRepository.save(settings);
                }
                return ResponseEntity.ok(new SettingsResponseBody(multiuserMode, postPremoderation, statisticsIsPublic));
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @Override
    public ResponseEntity<StatisticResponseBody> getMyStatistics()
    {
        if (authService.isUserAuthorize()) {
            User user = authService.getAuthorizedUser();
            byte isActive = utilitiesService.getIsActive();
            ModerationStatus moderationStatus = ModerationStatus.valueOf(utilitiesService.getModerationStatus());
            LocalDateTime time = utilitiesService.getTime();

            List<Post> posts = postRepository.findPostsByUser(isActive, moderationStatus, time, user, Sort.by("time"));
            return new ResponseEntity<>(createStatisticResponseBody(posts, utilitiesService), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @Override
    public ResponseEntity<StatisticResponseBody> getAllStatistics(Principal principal)
    {
        GlobalSettings settings = globalSettingsRepository.findByCode("STATISTICS_IS_PUBLIC");
        User user = principal != null ? authService.getAuthorizedUser() : null;

        if ((settings.getValue().equals("NO") && user == null)
                || (settings.getValue().equals("NO") && user != null && user.getIsModerator() == 0)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        byte isActive = utilitiesService.getIsActive();
        ModerationStatus moderationStatus = ModerationStatus.valueOf(utilitiesService.getModerationStatus());
        LocalDateTime time = utilitiesService.getTime();

        List<Post> posts = postRepository.findSortPosts(isActive, moderationStatus, time, Sort.by("time"));
        return new ResponseEntity<>(createStatisticResponseBody(posts, utilitiesService), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseBody> addComment(ApiRequestBody comment) {
        if (!authService.isUserAuthorize())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Post post = postRepository.findById(comment.postId());
        User user = authService.getAuthorizedUser();

        if (post == null)
            return ResponseEntity.badRequest().body(utilitiesService.getErrorResponse(ErrorsBody.builder()
                            .text(Errors.POST_FOR_COMMENT_IS_NOT_EXIST.getTitle())
                            .build()));

        if (comment.parentId() != null) {
            int parentId = comment.parentId();
            PostComment postComment = postCommentRepository.findById(parentId);
            if (postComment == null)
                return ResponseEntity.badRequest().body(utilitiesService.getErrorResponse(ErrorsBody.builder()
                                .text(Errors.COMMENT_FOR_ANSWER_IS_NOT_EXIST.getTitle())
                                .build()));
        }
        if (comment.text().length() < commentMinLength) {
            return ResponseEntity.ok(utilitiesService.getErrorResponse(ErrorsBody.builder()
                    .text(Errors.COMMENT_IS_EMPTY_OR_SHORT.getTitle())
                    .build()));
        }
        PostComment postComment = postCommentRepository.save(PostComment.builder()
                .parentId(comment.parentId())
                .post(post)
                .user(user)
                .time(utilitiesService.getTime())
                .text(comment.text())
                .build());
        return ResponseEntity.ok(ApiResponseBody.builder().id(postComment.getId()).result(true).build());
    }

    @Override
    public ResponseEntity<ApiResponseBody>moderation(int postId, String decision)
    {
        if (!authService.isUserAuthorize())
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        User user = authService.getAuthorizedUser();
        if (user.getIsModerator() != 1)
            return new ResponseEntity<>(ApiResponseBody.builder().result(false).build(), HttpStatus.OK);

        Post post = postRepository.findById(postId);

        if (decision.equals("accept"))
            post.setModerationStatus(ModerationStatus.ACCEPTED);
        else post.setModerationStatus(ModerationStatus.DECLINED);

        post.setModeratorId(user.getId());
        postRepository.save(post);

        return new ResponseEntity<>(ApiResponseBody.builder().result(true).build(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> imageUpload(MultipartFile multipartFile) throws IOException {

        if (authService.isUserAuthorize() && multipartFile != null) {

            String fileName = multipartFile.getOriginalFilename();
            int indexOfPoint = Objects.requireNonNull(fileName).lastIndexOf(".") + 1;
            String extension = Objects.requireNonNull(fileName).substring(indexOfPoint);
            fileName = utilitiesService.getRandomHash(6) + "." + extension;

            if (!extension.equalsIgnoreCase("jpg") && !extension.equalsIgnoreCase("png")) {
                return ResponseEntity.badRequest().body(utilitiesService.getErrorResponse(ErrorsBody.builder()
                                .image(Errors.IMAGE_INVALID_FORMAT.getTitle())
                                .build()));
            }
            if (multipartFile.getSize() > 5_000_000) {
                return ResponseEntity.badRequest().body(getBigImageErrorResponse());
            }
            String[] alphabet = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
                    "s", "t", "u", "v", "w", "x", "y", "z"};
            String newLocation = location;

            for (int i = 0; i < 3; i++) {
                String folderName = alphabet[new Random().nextInt(25)] + alphabet[new Random().nextInt(25)];
                newLocation = newLocation.concat("/" + folderName);
            }
            File dirs = new File(newLocation);
            if (dirs.mkdirs()) {
                BufferedImage bufferedImage = ImageIO.read(multipartFile.getInputStream());
                File outputFile = new File(String.format("%s/%s", dirs, fileName));
                ImageIO.write(bufferedImage, extension, outputFile);
            }
            return ResponseEntity.ok(newLocation + "/" + fileName);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    public ResponseEntity<ApiResponseBody> editProfileWithPhoto(String email, int removePhoto, MultipartFile file,
                                                                String name,String password) throws IOException {
        if (!authService.isUserAuthorize())
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        User user = authService.getAuthorizedUser();
        if (file.getSize() > 5_000_000) {
            return ResponseEntity.badRequest().body(getBigImageErrorResponse());
        }
        if (removePhoto == 0) {
            String avatarName = file.getOriginalFilename();
            int indexOfPoint = Objects.requireNonNull(avatarName).lastIndexOf(".") + 1;
            String extension = Objects.requireNonNull(avatarName).substring(indexOfPoint);
            avatarName = avatarPrefix + utilitiesService.getRandomHash(6) + "." + extension;
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            BufferedImage resizedAvatar = utilitiesService.imageResizer(bufferedImage);
            File outputFile = new File(String.format("%s/%s", location, avatarName));
            ImageIO.write(resizedAvatar, extension, outputFile);
            String photo = outputFile.getPath().substring(1);
            user.setPhoto(photo);
        }
        if (utilitiesService.isNameIncorrect(name))
            return ResponseEntity.badRequest().body(utilitiesService.getErrorResponse(ErrorsBody.builder()
                    .name(Errors.NAME_IS_INCORRECT.getTitle())
                    .build()));
        else user.setName(name);

        if (!email.equals(user.getEmail()))
            return ResponseEntity.badRequest().body(utilitiesService.getErrorResponse(ErrorsBody.builder()
                    .email(Errors.EMAIL_IS_INCORRECT.getTitle())
                    .build()));

        if (password != null) {
            if (utilitiesService.isPasswordShort(password))
                return ResponseEntity.badRequest().body(utilitiesService.getErrorResponse(ErrorsBody.builder()
                        .password(Errors.PASSWORD_IS_SHORT.getTitle())
                        .build()));
            else user.setPassword(utilitiesService.encodePassword(password));
        }
        userRepository.save(user);
        return ResponseEntity.ok().body(ApiResponseBody.builder().result(true).build());
    }

    public ResponseEntity<ApiResponseBody> editProfileWithoutPhoto(ApiRequestBody apiRequestBody) {
        if (!authService.isUserAuthorize())
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        User user = authService.getAuthorizedUser();
        String name = apiRequestBody.name();
        String email = apiRequestBody.email();
        String password = apiRequestBody.password();
        Integer removePhoto = apiRequestBody.removePhoto();
        String photo = apiRequestBody.photo();

        if (!email.equals(user.getEmail()))
            return ResponseEntity.badRequest().body(utilitiesService.getErrorResponse(ErrorsBody.builder()
                    .email(Errors.EMAIL_IS_INCORRECT.getTitle())
                    .build()));

        if (utilitiesService.isNameIncorrect(name))
            return ResponseEntity.badRequest().body(utilitiesService.getErrorResponse(ErrorsBody.builder()
                    .name(Errors.NAME_IS_INCORRECT.getTitle())
                    .build()));
        else user.setName(name);

        if (password != null) {
            if (utilitiesService.isPasswordShort(password))
                return ResponseEntity.badRequest().body(utilitiesService.getErrorResponse(ErrorsBody.builder()
                        .password(Errors.PASSWORD_IS_SHORT.getTitle())
                        .build()));
            else user.setPassword(utilitiesService.encodePassword(password));
        }
        if (removePhoto != null && removePhoto == 1)
            user.setPhoto(photo);

        userRepository.save(user);
        return ResponseEntity.ok().body(ApiResponseBody.builder().result(true).build());
    }

    private ApiResponseBody getBigImageErrorResponse() {
        return ApiResponseBody.builder()
                .result(false)
                .errors(ErrorsBody.builder()
                        .image(Errors.IMAGE_IS_BIG.getTitle())
                        .build())
                .build();
    }
}
