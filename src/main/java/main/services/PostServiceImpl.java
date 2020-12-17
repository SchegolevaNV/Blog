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
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService
{
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final PostVoteRepository postVoteRepository;
    private final TagToPostRepository tagToPostRepository;
    private final AuthService authService;
    private final UtilitiesService utilitiesService;
    private final GlobalSettingsRepository globalSettingsRepository;

    @Value("${announce.max.length}")
    private int announceMaxLength;

    @Value("${post.title.min.length}")
    private int postTitleMinLength;

    @Value("${post.text.min.length}")
    private int postTextMinLength;

    @Override
    public ResponseEntity<PostWallResponseBody> getAllPosts (int offset, int limit, String mode) {

        byte isActive = utilitiesService.getIsActive();
        ModerationStatus moderationStatus = ModerationStatus.valueOf(utilitiesService.getModerationStatus());
        LocalDateTime time = utilitiesService.getTime();

        List<Post> posts = getAndSortPosts(offset, limit, mode);
        int count = postRepository.getPostsCountByActiveAndModStatusAndTime(isActive, moderationStatus, time);
        return ResponseEntity.ok(new PostWallResponseBody(count, getListPostBodies(posts)));
    }

    @Override
    public ResponseEntity<PostWallResponseBody> searchPosts(int offset, int limit, String query) {

        byte isActive = utilitiesService.getIsActive();
        ModerationStatus moderationStatus = ModerationStatus.valueOf(utilitiesService.getModerationStatus());
        LocalDateTime time = utilitiesService.getTime();

        Pageable pageable = setPageable(offset, limit);
        List<Post> posts = postRepository.findPostByQuery(isActive, moderationStatus, time, query, pageable);
        int count = postRepository.getTotalPostCountByQuery(isActive, moderationStatus, time, query);
        return ResponseEntity.ok(new PostWallResponseBody(count, getListPostBodies(posts)));
    }

    @Override
    public ResponseEntity<PostWallResponseBody> getPostsByDate(int offset, int limit, String date) {

        byte isActive = utilitiesService.getIsActive();
        ModerationStatus moderationStatus = ModerationStatus.valueOf(utilitiesService.getModerationStatus());
        LocalDateTime time = utilitiesService.getTime();

        Pageable pageable = setPageable(offset, limit);
        List<Post> posts = postRepository.findPostByDate(isActive, moderationStatus, time, date, pageable);
        int count = postRepository.getTotalPostCountByDate(isActive, moderationStatus, time, date);
        return ResponseEntity.ok(new PostWallResponseBody(count, getListPostBodies(posts)));
    }

    @Override
    public ResponseEntity<PostWallResponseBody> getPostsByTag(int offset, int limit, String tag) {
        byte isActive = utilitiesService.getIsActive();
        ModerationStatus moderationStatus = ModerationStatus.valueOf(utilitiesService.getModerationStatus());
        LocalDateTime time = utilitiesService.getTime();
        List<Post> posts;
        int count;

        Tag myTag = tagRepository.findByName(tag);
        if (myTag == null) {
            count = 0;
            posts = new ArrayList<>();
            return ResponseEntity.ok(new PostWallResponseBody(count, getListPostBodies(posts)));
        }
        Pageable pageable = setPageable(offset, limit);
        count = postRepository.getTotalPostByTag(isActive, moderationStatus, time, myTag.getId());
        posts = postRepository.findAllPostByTag(isActive, moderationStatus, time, myTag.getId(), pageable);

        return ResponseEntity.ok(new PostWallResponseBody(count, getListPostBodies(posts)));
    }

    @Override
    public ResponseEntity<PostResponseBody> getPostById(int id, Principal principal)
    {
        User user = principal != null ? authService.getAuthorizedUser() : null;
        Post post = postRepository.findById(id);
        if (post == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        List<CommentBody> commentBodies = new ArrayList<>();
        List<String> tags = new ArrayList<>();

        List<Tag> tagList = post.getPostTags();
        tagList.forEach(tag -> tags.add(tag.getName()));

        List<PostComment> comments = post.getPostComments();
        for(PostComment comment : comments) {
            User commentUser = comment.getUser();
            long timestamp = utilitiesService.getTimestampFromLocalDateTime(comment.getTime());
            commentBodies.add(
                    new CommentBody(
                    comment.getId(),
                    timestamp,
                    comment.getText(),
                    UserBody.builder()
                            .id(commentUser.getId())
                            .name(commentUser.getName())
                            .photo(commentUser.getPhoto())
                            .build()));
        }
        if (user == null || (user != post.getUser() && user.getIsModerator() != 1)) {
            int viewCount = post.getViewCount();
            post.setViewCount(viewCount + 1);
            postRepository.save(post);
        }
        PostResponseBody postResponseBody = createPostResponseBody(post);
        postResponseBody.setComments(commentBodies);
        postResponseBody.setTags(tags);
        postResponseBody.setActive(post.getIsActive() == 1);
        postResponseBody.setText(post.getText());
        postResponseBody.setAnnounce(null);

        return ResponseEntity.ok(postResponseBody);
    }

    @Override
    public ResponseEntity<PostWallResponseBody> getPostsForModeration(int offset, int limit, String status) {
        if (authService.isUserAuthorize()) {
            User user = authService.getAuthorizedUser();
            if (user.getIsModerator() == 1) {
                ModerationStatus modStatus = ModerationStatus.valueOf(status.toUpperCase());
                Pageable pageable = setPageable(offset, limit);
                byte isActive = utilitiesService.getIsActive();
                List<Post> posts;
                int count;

                if (modStatus.toString().equals("NEW")) {
                    posts = postRepository.findByModerationStatusAndIsActive(modStatus, isActive, pageable);
                    count = postRepository.getPostsCountByActiveAndModStatus(isActive, modStatus);
                } else {
                    posts = postRepository.findByModerationStatusAndIsActiveAndModeratorId(
                            modStatus, isActive, user.getId(), pageable);
                    count = postRepository.getTotalPostsByModerator(isActive, modStatus, user.getId());
                }
                return ResponseEntity.ok(new PostWallResponseBody(count, getListPostBodies(posts)));
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @Override
    public ResponseEntity<PostWallResponseBody> getMyPosts(int offset, int limit, String status) {
        if (authService.isUserAuthorize())
        {
            byte isActive = utilitiesService.getIsActive();
            User user = authService.getAuthorizedUser();
            Pageable pageable = setPageable(offset, limit);
            List<Post> posts;
            int count;

            if (status.equals("inactive")) {
                posts = postRepository.findByIsActiveAndUser((byte) 0, user, pageable);
                count = postRepository.getTotalInactivePostsByUser(user);
            }
            else {
                if (status.equals("pending"))
                    status = ModerationStatus.NEW.toString();
                if (status.equals("published"))
                    status = ModerationStatus.ACCEPTED.toString();
                if (status.equals("declined"))
                    status = ModerationStatus.DECLINED.toString();

                posts = postRepository.findByIsActiveAndModerationStatusAndUser(isActive,
                        ModerationStatus.valueOf(status), user, pageable);
                count = postRepository.getTotalPostsCountByUser(isActive, ModerationStatus.valueOf(status), user);
            }
            return ResponseEntity.ok(new PostWallResponseBody(count, getListPostBodies(posts)));
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @Override
    public ResponseEntity<ApiResponseBody> postLikeOrDislike(int postId, byte usersPostVote)
    {
        if (authService.isUserAuthorize()) {
            User user = authService.getAuthorizedUser();
            Post post = postRepository.findById(postId);

            if (post.getUser() == user)
                return ResponseEntity.badRequest().body(utilitiesService.getErrorResponse(ErrorsBody.builder()
                                .text(Errors.YOU_WRONG.getTitle())
                                .build()));

            PostVote postVote = postVoteRepository.findByPostAndUser(post, user);
            LocalDateTime time = utilitiesService.getTime();

            if (postVote != null) {
                if (postVote.getValue() == usersPostVote) {
                    return ResponseEntity.ok(ApiResponseBody.builder().result(false).build());
                }
                else postVoteRepository.deleteById(postVote.getId());
            }
            postVoteRepository.save(PostVote.builder()
                    .user(user).post(post).time(time).value(usersPostVote).build());

            return ResponseEntity.ok(ApiResponseBody.builder().result(true).build());
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @Override
    public ResponseEntity<ApiResponseBody> addPost(long timestamp, byte active, String title, List<String> tags,
                                                   String text) {
        if (authService.isUserAuthorize()) {
            String postPremoderation = globalSettingsRepository.findByCode("POST_PREMODERATION").getValue();
            User user = authService.getAuthorizedUser();
            if (isTitleAndTextIncorrect(title, text))
                return ResponseEntity.ok(errorResponse());
            LocalDateTime dateTime = utilitiesService.getLocalDateTimeFromTimestamp(timestamp);
            Post newPost = postRepository.save(Post.builder()
                    .isActive(active)
                    .user(user)
                    .time(utilitiesService.setRightTime(dateTime))
                    .text(text)
                    .title(title)
                    .viewCount(0)
                    .moderationStatus(postPremoderation.equals("YES") && user.getIsModerator() == 0
                            ? ModerationStatus.NEW
                            : ModerationStatus.ACCEPTED)
                    .build());

            if (!tags.isEmpty())
                updateTagsTables(tags, newPost);

            return ResponseEntity.ok(ApiResponseBody.builder().result(true).build());
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponseBody> editPost(int id, long timestamp, byte active, String title,
                                                    List<String> tags, String text)
    {
        if (authService.isUserAuthorize()) {
            String postPremoderation = globalSettingsRepository.findByCode("POST_PREMODERATION").getValue();
            Post post = postRepository.findById(id);
            if (post == null)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            if (isTitleAndTextIncorrect(title, text))
                return ResponseEntity.ok(errorResponse());

            User user = authService.getAuthorizedUser();
            ModerationStatus status = post.getModerationStatus();

            if (post.getUser() == user && user.getIsModerator() == 0 && postPremoderation.equals("YES"))
                status = ModerationStatus.NEW;

                LocalDateTime dateTime = utilitiesService.getLocalDateTimeFromTimestamp(timestamp);
                post.setIsActive(active);
                post.setModerationStatus(status);
                post.setTime(utilitiesService.setRightTime(dateTime));
                post.setTitle(title);
                post.setText(text);

                postRepository.save(post);
                tagToPostRepository.deleteByPost(post);

                if (!tags.isEmpty())
                    updateTagsTables(tags, post);

            return ResponseEntity.ok(ApiResponseBody.builder().result(true).build());
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    /** private methods*/

    private void updateTagsTables (List<String> tags, Post post) {
        for (String tagName : tags) {
            Tag tag = tagRepository.findByName(tagName);
            if (tag == null)
                tag = tagRepository.save(new Tag(tagName));
            tagToPostRepository.save(TagToPost.builder().post(post).tag(tag).build());
        }
    }

    private List<Post> getAndSortPosts(int offset, int limit, String mode) {

        int page = offset/limit;
        byte isActive = utilitiesService.getIsActive();
        ModerationStatus moderationStatus = ModerationStatus.valueOf(utilitiesService.getModerationStatus());
        LocalDateTime time = utilitiesService.getTime();
        List<Post> posts;

        if (mode.equals(ModeValue.popular.toString())) {
            Pageable pageable = PageRequest.of(page, limit, Sort.by("commentsCount").descending());
            posts = postRepository.findAllPostSortedByComments(isActive, moderationStatus, time, pageable);
        }
        else if (mode.equals(ModeValue.best.toString())) {
            Pageable pageable = PageRequest.of(page, limit);
            posts = postRepository.findAllPostSortedByLikes(isActive, moderationStatus, time, pageable);
        }
        else {
            Pageable pageable = PageRequest.of(page, limit, Sort.by("time"));
            if (mode.equals(ModeValue.recent.toString()))
                pageable = PageRequest.of(page, limit, Sort.by("time").descending());
            posts = postRepository.findPostByIsActiveAndModerationStatusAndTimeBefore(isActive, moderationStatus,
                    time, pageable);
        }
        return posts;
    }

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
        String announce = Jsoup.parse(post.getText()).text();
        return (announce.length() > announceMaxLength)
                ? announce.substring(0, announceMaxLength - 1) + "..."
                : announce;
    }

    private boolean isTitleAndTextIncorrect(String title, String text) {
        return title == null
                || text == null
                || title.length() < postTitleMinLength
                || text.length() < postTextMinLength;
    }

    private ApiResponseBody errorResponse () {
        ErrorsBody error = ErrorsBody.builder()
                .title(Errors.TITLE_IS_NOT_SET_OR_SHORT.getTitle())
                .text(Errors.TEXT_IS_NOT_SET_OT_SHORT.getTitle())
                .build();
        return ApiResponseBody.builder().result(false).errors(error).build();
    }

    private Pageable setPageable(int offset, int limit) {
        int page = offset/limit;
        return PageRequest.of(page, limit);
    }
}