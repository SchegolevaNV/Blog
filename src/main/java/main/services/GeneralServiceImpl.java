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
import javax.transaction.Transactional;
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

    @Override
    public ResponseEntity<TagsResponseBody> getTags(String query) {

        byte isActive = utilitiesService.getIsActive();
        ModerationStatus moderationStatus = utilitiesService.getModerationStatus();
        LocalDateTime time = utilitiesService.getTime();

        int count = postRepository.getPostsCountByActiveAndModStatusAndTime(isActive, moderationStatus, time);
        List<TagsBody> tags = new ArrayList<>();
        if (query == null)
            query = "";

        List<Tag> tagList = tagRepository.findByNameStartingWith(query);
        for (Tag tag : tagList) {
            int postsCount = postRepository.getTotalPostByTag(isActive, moderationStatus, time, tag.getId());
            double weight = (double) postsCount / (double) count;
            tags.add(new TagsBody(tag.getName(), weight));
        }
        return ResponseEntity.ok(new TagsResponseBody(tags));
    }

    @Override
    public ResponseEntity<CalendarResponseBody> getCalendar(String year)
    {
        LocalDateTime currentTime = utilitiesService.getTime();
        byte isActive = utilitiesService.getIsActive();
        ModerationStatus moderationStatus = utilitiesService.getModerationStatus();

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

        for (GlobalSettings mySettings : globalSettings) {
            boolean value = false;

            if (mySettings.getValue().equals("YES"))
                value = true;

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
    public ResponseEntity<SettingsResponseBody> putSettings(boolean multiuserMode, boolean postPremoderation, boolean statisticsIsPublic)
    {
        String value = "NO";
        HashMap<String, Boolean> codes = new HashMap<>();
        codes.put(Settings.MULTIUSER_MODE.toString(), multiuserMode);
        codes.put(Settings.POST_PREMODERATION.toString(), postPremoderation);
        codes.put(Settings.STATISTICS_IS_PUBLIC.toString(), statisticsIsPublic);

        if (authService.isUserAuthorize()) {
            User user = authService.getAuthorizedUser();
            if (user.getIsModerator() == 1) {
                for (Map.Entry<String, Boolean> code : codes.entrySet()) {
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
        return null;
    }

    @Override
    public ResponseEntity<StatisticResponseBody> getMyStatistics()
    {
        if (authService.isUserAuthorize())
        {
            User user = authService.getAuthorizedUser();
            byte isActive = utilitiesService.getIsActive();
            ModerationStatus moderationStatus = utilitiesService.getModerationStatus();
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
        User user = null;

        if(principal != null)
            user = authService.getAuthorizedUser();

        if ((settings.getValue().equals("NO") && user == null)
                || (settings.getValue().equals("NO") && user != null && user.getIsModerator() == 0)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        byte isActive = utilitiesService.getIsActive();
        ModerationStatus moderationStatus = utilitiesService.getModerationStatus();
        LocalDateTime time = utilitiesService.getTime();

        List<Post> posts = postRepository.findSortPosts(isActive, moderationStatus, time, Sort.by("time"));
        return new ResponseEntity<>(createStatisticResponseBody(posts, utilitiesService), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseBody> addComment(ApiRequestBody comment)
    {
        if (!authService.isUserAuthorize())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Post post = postRepository.findById(comment.getPostId());
        User user = authService.getAuthorizedUser();

        if (post == null)
            return new ResponseEntity(Errors.POST_FOR_COMMENT_IS_NOT_EXIST.getTitle(), HttpStatus.BAD_REQUEST);

        if (comment.getParentId() != null) {
            int parentId = comment.getParentId();
            PostComment postComment = postCommentRepository.findById(parentId);
            if (postComment == null)
                return new ResponseEntity(Errors.COMMENT_FOR_ANSWER_IS_NOT_EXIST.getTitle(), HttpStatus.BAD_REQUEST);
        }

        if (comment.getText().length() < commentMinLength) {
            ApiResponseBody responseBody = ApiResponseBody.builder()
                    .result(false)
                    .errors(ErrorsBody.builder()
                            .text(Errors.COMMENT_IS_EMPTY_OR_SHORT.getTitle())
                            .build())
                    .build();
            return ResponseEntity.ok(responseBody);
        }

        PostComment postComment = postCommentRepository.save(PostComment.builder()
                .parentId(comment.getParentId())
                .post(post)
                .user(user)
                .time(utilitiesService.getTime())
                .text(comment.getText())
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
    public ResponseEntity imageUpload(MultipartFile multipartFile) throws IOException {

        if (authService.isUserAuthorize() && multipartFile !=null) {

            String fileName = multipartFile.getOriginalFilename();
            String extension = Objects.requireNonNull(fileName).split("\\.")[1];

            if (!extension.equalsIgnoreCase("jpg") && !extension.equalsIgnoreCase("png")) {
                return ResponseEntity.badRequest().body(ApiResponseBody.builder()
                        .result(false)
                        .errors(ErrorsBody.builder()
                                .image(Errors.IMAGE_INVALID_FORMAT.getTitle())
                                .build())
                        .build());
            }

            if (multipartFile.getSize() > 5_000_000) {
                return ResponseEntity.badRequest().body(ApiResponseBody.builder()
                        .result(false)
                        .errors(ErrorsBody.builder()
                                .image(Errors.IMAGE_IS_BIG.getTitle())
                                .build())
                        .build());
            }

            String[] alphabet = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
                    "s", "t", "u", "v", "w", "x", "y", "z"};

            Random random = new Random();
            String newLocation = location;

            for (int i = 0; i < 3; i++) {
                String folderName = alphabet[random.nextInt(25)] + alphabet[random.nextInt(25)];
                newLocation = newLocation.concat("/" + folderName);
            }
            File dirs = new File(newLocation);
            dirs.mkdirs();
            BufferedImage bufferedImage = ImageIO.read(multipartFile.getInputStream());
            File outputFile = new File(dirs + "/" + fileName);
            ImageIO.write(bufferedImage, "jpg", outputFile);

            return ResponseEntity.ok(newLocation + "/" + fileName);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    public ResponseEntity<ApiResponseBody> editProfileWithPhoto(String email, int removePhoto, MultipartFile file,
                                                                String name,String password) throws IOException {
        if (!authService.isUserAuthorize())
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        User user = authService.getAuthorizedUser();
        
        if (removePhoto == 0) {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            BufferedImage resizedAvatar = utilitiesService.imageResizer(bufferedImage);
            String avatarName = file.getOriginalFilename();
            File outputFile = new File(location + "/" + avatarName);
            ImageIO.write(resizedAvatar, "jpg", outputFile);

            String photo = outputFile.getPath().substring(1);
            user.setPhoto(photo);
        }

        //TODO выделить проверки отдельно
        if (!utilitiesService.isNameCorrect(name))
            return ResponseEntity.badRequest().body(ApiResponseBody.builder()
                    .result(false)
                    .errors(ErrorsBody.builder()
                            .image(Errors.NAME_IS_INCORRECT.getTitle())
                            .build())
                    .build());
        else user.setName(name);

        if (userRepository.findByEmail(email) != null && !email.equals(user.getEmail()))
            return ResponseEntity.badRequest().body(ApiResponseBody.builder()
                    .result(false)
                    .errors(ErrorsBody.builder()
                            .image(Errors.THIS_EMAIL_IS_EXIST.getTitle())
                            .build())
                    .build());
        else if (!utilitiesService.isEmailCorrect(email))
            return ResponseEntity.badRequest().body(ApiResponseBody.builder()
                    .result(false)
                    .errors(ErrorsBody.builder()
                            .image(Errors.EMAIL_IS_INCORRECT.getTitle())
                            .build())
                    .build());
        else user.setEmail(email);

        if (password != null) {
            if (!utilitiesService.isPasswordNotShort(password))
                return ResponseEntity.badRequest().body(ApiResponseBody.builder()
                        .result(false)
                        .errors(ErrorsBody.builder()
                                .image(Errors.PASSWORD_IS_SHORT.getTitle())
                                .build())
                        .build());
            else user.setPassword(utilitiesService.encodePassword(password));
        }

        userRepository.save(user);
        return ResponseEntity.ok().body(ApiResponseBody.builder().result(true).build());
    }

    public ResponseEntity<ApiResponseBody> editProfileWithoutPhoto(ApiRequestBody apiRequestBody) {
        if (!authService.isUserAuthorize())
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        User user = authService.getAuthorizedUser();
        String name = apiRequestBody.getName();
        String email = apiRequestBody.getEmail();
        String password = apiRequestBody.getPassword();
        Integer removePhoto = apiRequestBody.getRemovePhoto();
        String photo = apiRequestBody.getPhoto();

        if (!utilitiesService.isNameCorrect(name))
            return ResponseEntity.badRequest().body(ApiResponseBody.builder()
                    .result(false)
                    .errors(ErrorsBody.builder()
                            .image(Errors.NAME_IS_INCORRECT.getTitle())
                            .build())
                    .build());
        else user.setName(name);

        if (userRepository.findByEmail(email) != null && !email.equals(user.getEmail()))
            return ResponseEntity.badRequest().body(ApiResponseBody.builder()
                    .result(false)
                    .errors(ErrorsBody.builder()
                            .image(Errors.THIS_EMAIL_IS_EXIST.getTitle())
                            .build())
                    .build());
        else if (!utilitiesService.isEmailCorrect(email))
            return ResponseEntity.badRequest().body(ApiResponseBody.builder()
                    .result(false)
                    .errors(ErrorsBody.builder()
                            .image(Errors.EMAIL_IS_INCORRECT.getTitle())
                            .build())
                    .build());
        else user.setEmail(email);

        if (password != null) {
            if (!utilitiesService.isPasswordNotShort(password))
                return ResponseEntity.badRequest().body(ApiResponseBody.builder()
                        .result(false)
                        .errors(ErrorsBody.builder()
                                .image(Errors.PASSWORD_IS_SHORT.getTitle())
                                .build())
                        .build());
            else user.setPassword(utilitiesService.encodePassword(password));
        }
        if (removePhoto != null && removePhoto == 1)
            user.setPhoto(photo);

        userRepository.save(user);
        return ResponseEntity.ok().body(ApiResponseBody.builder().result(true).build());
    }
}
