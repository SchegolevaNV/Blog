package main.services;

import lombok.RequiredArgsConstructor;
import main.api.responses.ApiResponseBody;
import main.api.responses.PostResponseBody;
import main.api.responses.PostWallResponseBody;
import main.api.responses.bodies.ErrorsBody;
import main.model.*;
import main.model.enums.Errors;
import main.model.enums.ModeValue;
import main.model.enums.ModerationStatus;
import main.repositories.*;
import main.api.responses.bodies.CommentBody;
import main.api.responses.bodies.UserBody;
import main.services.interfaces.AuthService;
import main.services.interfaces.PostService;
import main.services.interfaces.UtilitiesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static main.model.enums.ModerationStatus.NEW;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:constants.yml")
public class PostServiceImpl implements PostService
{
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostVoteRepository postVoteRepository;
    private final TagToPostRepository tagToPostRepository;
    private final AuthService authService;
    private final UtilitiesService utilitiesService;

    @Value("${is.active}")
    private byte isActive;

    @Value("${moderation.status}")
    private ModerationStatus moderationStatus;

    @Value("${announce.max.length}")
    private int announceMaxLength;

    @Value("${post.title.min.length}")
    private int postTitleMinLength;

    @Value("${post.title.max.length}")
    private int postTitleMaxLength;

    private LocalDateTime time = LocalDateTime.now(ZoneId.of("UTC"));
    private List<Post> posts;

    @Override
    public ResponseEntity<PostWallResponseBody> getAllPosts (int offset, int limit, String mode) {
        posts = getAndSortPosts(offset, limit, mode);
        int count = postRepository.getPostsCountByActiveAndModStatusAndTime(isActive, moderationStatus, time);
        return ResponseEntity.ok(new PostWallResponseBody(count, getListPostBodies(posts)));
    }

    @Override
    public ResponseEntity<PostWallResponseBody> searchPosts(int offset, int limit, String query) {
        Pageable pageable = setPageable(offset, limit);
        posts = postRepository.findPostByQuery(isActive, moderationStatus, time, query, pageable);
        int count = postRepository.getTotalPostCountByQuery(isActive, moderationStatus, time, query);
        return ResponseEntity.ok(new PostWallResponseBody(count, getListPostBodies(posts)));
    }

    @Override
    public ResponseEntity<PostWallResponseBody> getPostsByDate(int offset, int limit, String date) {
        Pageable pageable = setPageable(offset, limit);
        posts = postRepository.findPostByDate(isActive, moderationStatus, time, date, pageable);
        int count = postRepository.getTotalPostCountByDate(isActive, moderationStatus, time, date);
        return ResponseEntity.ok(new PostWallResponseBody(count, getListPostBodies(posts)));
    }

    @Override
    public ResponseEntity<PostWallResponseBody> getPostsByTag(int offset, int limit, String tag)
    {
        int tagId = tagRepository.findByName(tag).getId();
        Pageable pageable = setPageable(offset, limit);
        int count = postRepository.getTotalPostByTag(isActive, moderationStatus, time, tagId);
        posts = postRepository.findAllPostByTag(isActive, moderationStatus, time, tagId, pageable);

        return ResponseEntity.ok(new PostWallResponseBody(count, getListPostBodies(posts)));
    }

    @Override
    public ResponseEntity<PostResponseBody> getPostById(int id)
    {
        List<CommentBody> commentBodies = new ArrayList<>();
        List<String> tags = new ArrayList<>();

        Post post = postRepository.findById(id);

        List<Tag> tagList = post.getPostTags();
        for (Tag tag : tagList) {
            tags.add(tag.getName());
        }

        List<PostComment> comments = post.getPostComments();
        for(PostComment comment : comments) {
            User commentUser = comment.getUser();
            commentBodies.add(new CommentBody(comment.getId(),
                    utilitiesService.getTimestampFromLocalDateTime(comment.getTime()),
                    comment.getText(),
                    UserBody.builder().id(commentUser.getId())
                            .name(commentUser.getName())
                            .photo(commentUser.getPhoto()).build()));
        }
        PostResponseBody postResponseBody = createPostResponseBody(post);
        postResponseBody.setComments(commentBodies);
        postResponseBody.setTags(tags);

        return ResponseEntity.ok(postResponseBody);
    }

    @Override
    public ResponseEntity<PostWallResponseBody> getPostsForModeration(int offset, int limit, String status)
    {
        status = ModerationStatus.valueOf(status.toUpperCase()).toString();
        int count;

        if (authService.isUserAuthorize())
        {
            User user = authService.getAuthorizedUser();
            if (user.getIsModerator() != 1)
                return new ResponseEntity("You're not the moderator!", HttpStatus.BAD_REQUEST);

            if (status.equals("NEW")) {
                posts = postRepository.findByModerationStatusAndIsActive(NEW, isActive, setPageable(offset, limit));
                count = postRepository.getPostsCountByActiveAndModStatus(isActive, NEW);
            }
            else {
                posts = postRepository.findByModerationStatusAndIsActiveAndModeratorId(ModerationStatus.valueOf(status),
                        isActive, user.getId(),
                        setPageable(offset, limit));
                count = postRepository.getTotalPostsByModerator(isActive, ModerationStatus.valueOf(status), user.getId());
            }

            List<PostResponseBody> postsList = new ArrayList<>();
            for (Post post : posts) {
                postsList.add(PostResponseBody.builder()
                        .id(post.getId())
                        .timestamp(utilitiesService.getTimestampFromLocalDateTime(post.getTime()))
                        .user(UserBody.builder().id(post.getUser().getId()).name(post.getUser().getName()).build())
                        .title(post.getTitle())
                        .announce(getAnnounce(post)).build());
            }
            return new ResponseEntity<>(new PostWallResponseBody(count, postsList), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @Override
    public ResponseEntity<PostWallResponseBody> getMyPosts(int offset, int limit, String status)
    {
        int count = 0;

        if (authService.isUserAuthorize())
        {
            User user = authService.getAuthorizedUser();
            if (status.equals("inactive")) {
                posts = postRepository.findByIsActiveAndUser((byte) 0, user, setPageable(offset, limit));
                count = postRepository.getTotalInactivePostsByUser(user);
            }
            else {
                if (status.equals("pending"))
                    status = NEW.toString();
                if (status.equals("published"))
                    status = ModerationStatus.ACCEPTED.toString();
                if (status.equals("declined"))
                    status = ModerationStatus.DECLINED.toString();

                posts = postRepository.findByIsActiveAndModerationStatusAndUser(isActive,
                        ModerationStatus.valueOf(status), user, setPageable(offset, limit));
                count = postRepository.getTotalPostsCountByUser(isActive, ModerationStatus.valueOf(status), user);
            }
            return new ResponseEntity<>(new PostWallResponseBody(count, getListPostBodies(posts)),
                    HttpStatus.OK);
        }
        else return null;
    }

    @Override
    public ResponseEntity<ApiResponseBody> postLike(int postId)
    {
        if (authService.isUserAuthorize())
        {
            User user = authService.getAuthorizedUser();
            Post post = postRepository.findById(postId);
            PostVote postVote = postVoteRepository.findByPostAndUser(post, user);

            if (postVote != null) {
                if (postVote.getValue() == 1) {
                    return ResponseEntity.ok(ApiResponseBody.builder().result(false).build());
                }
                else postVoteRepository.deleteById(postVote.getId());
            }
            postVoteRepository.save(PostVote.builder()
                    .user(user).post(post).time(time).value((byte)1).build());
        }
        return ResponseEntity.ok(ApiResponseBody.builder().result(true).build());
    }

    @Override
    public ResponseEntity<ApiResponseBody> postDislike(int postId) {
        if (authService.isUserAuthorize())
        {
            User user = authService.getAuthorizedUser();
            Post post = postRepository.findById(postId);
            PostVote postVote = postVoteRepository.findByPostAndUser(post, user);

            if (postVote != null)
            {
                if (postVote.getValue() == 0) {
                    return ResponseEntity.ok(ApiResponseBody.builder().result(false).build());
                }
                else postVoteRepository.deleteById(postVote.getId());
            }
            postVoteRepository.save(PostVote.builder()
                    .user(user).post(post).time(time).value((byte)0).build());
        }
        return ResponseEntity.ok(ApiResponseBody.builder().result(true).build());
    }

    @Override
    public ResponseEntity<ApiResponseBody> addPost(PostResponseBody post)
    {
        if (authService.isUserAuthorize()) {
            User user = authService.getAuthorizedUser();
            if (isTitleAndTextCorrect(post))
                return ResponseEntity.ok(errorResponse());

            else
            {
                Post newPost = postRepository.save(Post.builder()
                        .isActive(post.getActive())
                        .user(user)
                        .time(utilitiesService.setRightTime(time))
                        .text(post.getText()).title(post.getTitle())
                        .viewCount(0).moderationStatus(NEW).build());

                List<String> tags = post.getTags();
                if (!tags.isEmpty())
                    updateTagsTables(tags, newPost);
            }
            return ResponseEntity.ok(ApiResponseBody.builder().result(true).build());
        }
        return ResponseEntity.ok(ApiResponseBody.builder()
                .result(false)
                .build());
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponseBody> editPost(int id, PostResponseBody postResponseBody)
    {
        if (authService.isUserAuthorize())
        {
            Post post = postRepository.findById(id);
            User user = authService.getAuthorizedUser();
            ModerationStatus status = post.getModerationStatus();

            if (user.getIsModerator() == 0 || user.getId() != post.getModeratorId())
                status = NEW;

            if (isTitleAndTextCorrect(postResponseBody))
                return ResponseEntity.ok(errorResponse());

            else
            {
                post.setIsActive(postResponseBody.getActive());
                post.setModerationStatus(status);
                post.setTime(utilitiesService.getLocalDateTimeFromTimestamp(postResponseBody.getTimestamp()));
                post.setTitle(postResponseBody.getTitle());
                post.setText(postResponseBody.getText());

                postRepository.save(post);

                List<String> newTags = postResponseBody.getTags();
                tagToPostRepository.deleteByPost(post);

                if (!newTags.isEmpty())
                    updateTagsTables(newTags, post);
            }
            return ResponseEntity.ok(ApiResponseBody.builder().result(true).build());
        }
        return null;
    }

    private void updateTagsTables (List<String> tags, Post post)
    {
        for (String tagName : tags) {
            Tag tag = tagRepository.findByName(tagName);
            if (tag == null)
                tagRepository.save(new Tag(tagName));
            tagToPostRepository.save(TagToPost.builder().post(post).tag(tag).build());
        }
    }

    private List<Post> getAndSortPosts(int offset, int limit, String mode)
    {
        int page = offset/limit;

        if (mode.equals(ModeValue.popular.toString())) {
            Pageable pageable = PageRequest.of(page, limit, Sort.by("commentsCount").descending());
            posts = postRepository.findAllPostSortedByComments(isActive, moderationStatus, time, pageable);
        }
        else if (mode.equals(ModeValue.best.toString())) {
            Pageable pageable = PageRequest.of(page, limit);
            posts = postRepository.findAllPostSortedByLikes(isActive, moderationStatus, time, pageable);
        }
        else if (mode.equals(ModeValue.recent.toString())) {
            Pageable pageable = PageRequest.of(page, limit, Sort.by("time").descending());
            posts = postRepository.findPostByIsActiveAndModerationStatusAndTimeBefore(isActive, moderationStatus, time, pageable);
        }
        else {
            Pageable pageable = PageRequest.of(page, limit, Sort.by("time"));
            posts = postRepository.findPostByIsActiveAndModerationStatusAndTimeBefore(isActive, moderationStatus, time, pageable);
        }
        return posts;
    }

    /** private methods*/

    private List<PostResponseBody> getListPostBodies(List<Post> posts) {
        List<PostResponseBody> postBodies = new ArrayList<>();
        for (Post post : posts) {
            postBodies.add(createPostResponseBody(post));
        }
        return postBodies;
    }

    private PostResponseBody createPostResponseBody(Post post) {
        String likes = "likes";
        String dislikes = "dislikes";
        LocalDateTime postTimeToUtc = utilitiesService.convertLocalTimeToUtc(post.getTime());
        long timestamp = utilitiesService.getTimestampFromLocalDateTime(postTimeToUtc);

        return PostResponseBody.builder()
                .id(post.getId())
                .timestamp(timestamp)
                .user(UserBody.builder()
                        .id(post.getUser().getId())
                        .name(post.getUser().getName())
                        .build())
                .title(post.getTitle())
                .announce(getAnnounce(post))
                .likeCount(post.getVotesCount(likes))
                .dislikeCount(post.getVotesCount(dislikes))
                .commentCount(post.getCommentsCount())
                .viewCount(post.getViewCount())
                .build();
    }

    private String getAnnounce(Post post) {
        String announce = "";
        if (post.getText().length() > announceMaxLength) {
            announce = post.getText().substring(0, announceMaxLength - 1) + "...";
        } else announce = post.getText();
        return announce;
    }

    private boolean isTitleAndTextCorrect(PostResponseBody postResponseBody)
    {
        return postResponseBody.getTitle() == null
                || postResponseBody.getText() == null
                || postResponseBody.getTitle().length() < postTitleMinLength
                || postResponseBody.getText().length() < postTitleMaxLength;
    }

    // TODO переделать метод, сделать проверки и выдавать результат зависимо от, возможно, перенести в утилиты
    private ApiResponseBody errorResponse () {
        ErrorsBody error = ErrorsBody.builder()
                .title(Errors.TITLE_IS_NOT_SET.getTitle())
                .text(Errors.TEXT_IS_SHORT.getTitle())
                .build();

        return ApiResponseBody.builder().result(false).errors(error).build();
    }

    private Pageable setPageable(int offset, int limit) {
        int page = offset/limit;
        return PageRequest.of(page, limit);
    }
}