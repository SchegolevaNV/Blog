package main.services;

import main.api.requests.ApiRequestBody;
import main.api.responses.*;
import main.model.*;
import main.model.enums.Errors;
import main.model.enums.ModerationStatus;
import main.repositories.*;
import main.services.bodies.ErrorsBody;
import main.services.bodies.TagsBody;
import main.services.interfaces.AuthService;
import main.services.interfaces.GeneralService;
import main.services.interfaces.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class GeneralServiceImpl implements GeneralService, QueryService {

    @Autowired
    TagRepository tagRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    PostCommentRepository postCommentRepository;

    @Autowired
    AuthService authService;

    @Autowired
    GlobalSettingsRepository globalSettingsRepository;

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
        years.sort(Collections.reverseOrder());

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

        if (authService.isUserAuthorize())
        {
            User user = userRepository.findById(authService.getAuthorizedUserId());
            if (user.getIsModerator() == 1)
            {
                List<GlobalSettings> globalSettings = globalSettingsRepository.findAll();

                for (GlobalSettings mySettings : globalSettings)
                {
                    boolean value = false;

                    if (mySettings.getValue().equals("YES"))
                        value = true;

                    if (mySettings.getCode().equals("MULTIUSER_MODE"))
                        multiuserMode = value;
                    if (mySettings.getCode().equals("POST_PREMODERATION"))
                        postPremoderation = value;
                    if (mySettings.getCode().equals("STATISTICS_IS_PUBLIC"))
                        statisticsIsPublic = value;
                }
                return new SettingsResponseBody(multiuserMode, postPremoderation, statisticsIsPublic);
            }
        }
        return null;
    }

    @Override
    @Transactional
    public SettingsResponseBody putSettings(boolean multiuserMode, boolean postPremoderation, boolean statisticsIsPublic)
    {
        String value = "NO";
        HashMap<String, Boolean> codes = new HashMap<>();
        codes.put("MULTIUSER_MODE", multiuserMode);
        codes.put("POST_PREMODERATION", postPremoderation);
        codes.put("STATISTICS_IS_PUBLIC", statisticsIsPublic);

        if (authService.isUserAuthorize()) {
            User user = userRepository.findById(authService.getAuthorizedUserId());
            if (user.getIsModerator() == 1) {
                for (Map.Entry<String, Boolean> code : codes.entrySet()) {
                    if (code.getValue())
                        value = "YES";

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
            User user = userRepository.findById(authService.getAuthorizedUserId());
            List<Post> posts = postRepository.findPostsByUser((byte) 1, ModerationStatus.ACCEPTED, LocalDateTime.now(),
                    user, Sort.by("time"));
            return new ResponseEntity<>(createStatisticResponseBody(posts), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @Override
    public ResponseEntity<StatisticResponseBody> getAllStatistics()
    {
        GlobalSettings settings = globalSettingsRepository.findByCode("STATISTICS_IS_PUBLIC");

        if (!settings.getValue().equals("YES"))
            return new ResponseEntity("Statistic is restricted", HttpStatus.BAD_REQUEST);

        if(!authService.isUserAuthorize())
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        List<Post> posts = postRepository.findSortPosts((byte) 1, ModerationStatus.ACCEPTED, LocalDateTime.now(),
                Sort.by("time"));
        return new ResponseEntity<>(createStatisticResponseBody(posts), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseBody> addComment(ApiRequestBody comment)
    {
        if (!authService.isUserAuthorize())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Post post = postRepository.findById(comment.getPost_id());
        User user = userRepository.findById(authService.getAuthorizedUserId());

        if (comment.getParent_id() != null) {
            Optional<PostComment> postComment = postCommentRepository.findById(comment.getParent_id());
            if (postComment.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (post == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        if (comment.getText().length() < 10) {
            ApiResponseBody responseBody = ApiResponseBody.builder().result(false)
                    .errors(ErrorsBody.builder().text(Errors.CommentIsEmptyOrShort.getTitle())
                            .build()).build();
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        }

        PostComment postComment = postCommentRepository.save(PostComment.builder().parentId(comment.getParent_id())
                .post(post).user(user).time(LocalDateTime.now()).text(comment.getText()).build());
        return new ResponseEntity<>(ApiResponseBody.builder().id(postComment.getId()).result(true).build(), HttpStatus.OK);
    }

    @Override
    public ApiResponseBody editProfile() {

        return null;
    }

    @Override
    public ApiResponseBody moderation(ApiRequestBody post) {
        return null;
    }
}
