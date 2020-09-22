package main.services;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class GeneralServiceImpl implements GeneralService {

    private final TagRepository tagRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final AuthService authService;
    private final GlobalSettingsRepository globalSettingsRepository;
    private final UtilitiesService utilitiesService;

    public GeneralServiceImpl(TagRepository tagRepository,
                              PostRepository postRepository,
                              PostCommentRepository postCommentRepository,
                              AuthService authService,
                              GlobalSettingsRepository globalSettingsRepository,
                              UtilitiesService utilitiesService) {
        this.tagRepository = tagRepository;
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
        this.authService = authService;
        this.globalSettingsRepository = globalSettingsRepository;
        this.utilitiesService = utilitiesService;
    }

    @Value("${storage.location}")
    private String location;

    @Override
    public TagsResponseBody getTags(String query) {

        int count = postRepository.getPostsCountByActiveAndModStatusAndTime((byte) 1, ModerationStatus.ACCEPTED, LocalDateTime.now());
        List<TagsBody> tags = new ArrayList<>();
        if (query == null)
            query = "";

        List<Tag> tagList = tagRepository.findByNameStartingWith(query);
        for (Tag tag : tagList)
        {
            List<Post> posts = tag.getTagsPosts();
            posts.removeIf(post -> post.getIsActive() != 1
                    && post.getModerationStatus() != ModerationStatus.ACCEPTED
                    && !post.getTime().isBefore(LocalDateTime.now()));

            double weight = (double) posts.size() / (double) count;
            tags.add(new TagsBody(tag.getName(), weight));
        }
        return new TagsResponseBody(tags);
    }

    @Override
    public CalendarResponseBody getCalendar(String year)
    {
        if (year == null
                || !year.matches("[0-9]{4}")
                || Integer.parseInt(year) < 2015
                || Integer.parseInt(year) > LocalDate.now().getYear())
            year = Integer.toString(LocalDate.now().getYear());

        List<Integer> years = postRepository.getYears((byte) 1, ModerationStatus.ACCEPTED, LocalDateTime.now());

        TreeMap<String, Long> posts = new TreeMap<>();

        List<Object[]> postsInYear = postRepository.getPostCountInYearGroupByDate((byte) 1, ModerationStatus.ACCEPTED,
                LocalDateTime.now(), Integer.parseInt(year));

        postsInYear.forEach(postInYear -> {
            String day = postInYear[1].toString();
            Long count = (Long) postInYear[0];
            posts.put(day,count);
        });

        return new CalendarResponseBody(years, posts);
    }

    @Override
    public SettingsResponseBody getSettings()
    {
        boolean multiuserMode = false;
        boolean postPremoderation = false;
        boolean statisticsIsPublic = false;

        List<GlobalSettings> globalSettings = globalSettingsRepository.findAll();

        for (GlobalSettings mySettings : globalSettings)
        {
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
        return new SettingsResponseBody(multiuserMode, postPremoderation, statisticsIsPublic);
    }

    @Override
    @Transactional
    public SettingsResponseBody putSettings(boolean multiuserMode, boolean postPremoderation, boolean statisticsIsPublic)
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
                return new SettingsResponseBody(multiuserMode, postPremoderation, statisticsIsPublic);
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
            List<Post> posts = postRepository.findPostsByUser((byte) 1, ModerationStatus.ACCEPTED, LocalDateTime.now(),
                    user, Sort.by("time"));
            return new ResponseEntity<>(createStatisticResponseBody(posts, utilitiesService), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @Override
    public ResponseEntity<StatisticResponseBody> getAllStatistics()
    {
        GlobalSettings settings = globalSettingsRepository.findByCode("STATISTICS_IS_PUBLIC");

        if (!settings.getValue().equals("YES") && !authService.isUserAuthorize())
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        List<Post> posts = postRepository.findSortPosts((byte) 1, ModerationStatus.ACCEPTED, LocalDateTime.now(),
                Sort.by("time"));
        return new ResponseEntity<>(createStatisticResponseBody(posts, utilitiesService), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseBody> addComment(ApiRequestBody comment)
    {
        if (!authService.isUserAuthorize())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Post post = postRepository.findById(comment.getPostId());
        User user = authService.getAuthorizedUser();

        if (comment.getParentId() != null) {
            int parentId = comment.getParentId();
            PostComment postComment = postCommentRepository.findById(parentId);
            if (postComment == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (post == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        if (comment.getText().length() < 10) {
            ApiResponseBody responseBody = ApiResponseBody.builder().result(false)
                    .errors(ErrorsBody.builder().text(Errors.COMMENT_IS_EMPTY_OR_SHORT.getTitle())
                            .build()).build();
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }

        PostComment postComment = postCommentRepository.save(PostComment.builder().parentId(comment.getParentId())
                .post(post).user(user).time(LocalDateTime.now()).text(comment.getText()).build());
        return new ResponseEntity<>(ApiResponseBody.builder().id(postComment.getId()).result(true).build(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseBody>moderation(ApiRequestBody requestBody)
    {
        if (!authService.isUserAuthorize())
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        User user = authService.getAuthorizedUser();
        if (user.getIsModerator() != 1)
            return new ResponseEntity<>(ApiResponseBody.builder().result(false).build(), HttpStatus.OK);

        Post post = postRepository.findById(requestBody.getPostId());

        if (requestBody.getDecision().equals("accept"))
            post.setModerationStatus(ModerationStatus.ACCEPTED);
        else post.setModerationStatus(ModerationStatus.DECLINED);

        post.setModeratorId(user.getId());
        postRepository.save(post);

        return new ResponseEntity<>(ApiResponseBody.builder().result(true).build(), HttpStatus.OK);
    }

    @Override
    public ApiResponseBody editProfile() {

        return null;
    }

    @Override
    public ResponseEntity imageUpload(MultipartFile multipartFile) throws IOException {

        if (authService.isUserAuthorize() && multipartFile !=null) {

            String fileName = multipartFile.getOriginalFilename();
            assert fileName != null;
            String extension = fileName.substring(fileName.lastIndexOf('.') + 1);

            if (!extension.contentEquals("jpg") && !extension.contentEquals("png")) {
                return ResponseEntity.badRequest().body(ApiResponseBody.builder()
                        .result(false)
                        .errors(ErrorsBody.builder()
                                .image("Файл имеет некорректный формат")
                                .build())
                        .build());
            }

            if (multipartFile.getSize() > 5_000_000) {
                return ResponseEntity.badRequest().body(ApiResponseBody.builder()
                        .result(false)
                        .errors(ErrorsBody.builder()
                                .image("Размер файла превышает допустимый")
                                .build())
                        .build());
            }

            String[] alphabet = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
            "s", "t", "u", "v", "w", "x", "y", "z"};

            Random random = new Random();

            for (int i = 0; i < 3; i++) {
                String folderName = alphabet[random.nextInt(25)] + alphabet[random.nextInt(25)];
                location = location.concat("/" + folderName);
            }
            File dirs = new File(location);
            dirs.mkdirs();
            BufferedImage bufferedImage = ImageIO.read(multipartFile.getInputStream());
            File outputFile = new File(dirs + "/" + fileName);
            ImageIO.write(bufferedImage, "jpg", outputFile);

            return ResponseEntity.ok(location + "/" + fileName);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
