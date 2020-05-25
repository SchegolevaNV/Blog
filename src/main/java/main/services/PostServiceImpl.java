package main.services;

import main.api.responses.ApiResponseBody;
import main.api.responses.PostResponseBody;
import main.api.responses.PostWallResponseBody;
import main.model.*;
import main.model.enums.ModeValue;
import main.model.enums.ModerationStatus;
import main.repositories.*;
import main.services.bodies.CommentBody;
import main.services.bodies.UserBody;
import main.services.interfaces.AuthService;
import main.services.interfaces.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static main.model.enums.ModerationStatus.NEW;

@Service
public class PostServiceImpl implements PostService
{
    @Autowired
    PostRepository postRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostVoteRepository postVoteRepository;

    @Autowired
    TagToPostRepository tagToPostRepository;

    @Autowired
    AuthService authService;

    public static final byte IS_ACTIVE = 1;
    private static final ModerationStatus MODERATION_STATUS = ModerationStatus.ACCEPTED;
    private final LocalDateTime time = LocalDateTime.now();
    private List<Post> posts;

    @Override
    public PostWallResponseBody getAllPosts (int offset, int limit, String mode)
    {
        List<Post> posts = getAndSortPosts(offset, limit, mode);
        int count = postRepository.getPostsCountByActiveAndModStatusAndTime(IS_ACTIVE, MODERATION_STATUS, time);
        return new PostWallResponseBody(count, getListPostBodies(posts));
    }

    @Override
    public PostWallResponseBody searchPosts(int offset, int limit, String query)
    {
        posts = postRepository.findPostByQuery(IS_ACTIVE, MODERATION_STATUS, time, query, setPageable(offset, limit));
        int count = postRepository.getTotalPostCountByQuery(IS_ACTIVE, MODERATION_STATUS, time, query);
        return new PostWallResponseBody(count, getListPostBodies(posts));
    }

    @Override
    public PostWallResponseBody getPostsByDate(int offset, int limit, String date) {

        posts = postRepository.findPostByDate(IS_ACTIVE, MODERATION_STATUS, time, date, setPageable(offset, limit));
        int count = postRepository.getTotalPostCountByDate(IS_ACTIVE, MODERATION_STATUS, time, date);
        return new PostWallResponseBody(count, getListPostBodies(posts));
    }

    @Override
    public PostWallResponseBody getPostsByTag(int offset, int limit, String tag)
    {
        Tag myTag = tagRepository.findByName(tag);
        posts = myTag.getTagsPosts();

        posts.removeIf(post -> post.getIsActive() != 1
                && post.getModerationStatus() != ModerationStatus.ACCEPTED
                && !post.getTime().isBefore(LocalDateTime.now()));

        int finish = Math.min(posts.size(), offset + limit);

        return new PostWallResponseBody(posts.size(), getListPostBodies(posts.subList(offset, finish)));
    }

    @Override
    public PostResponseBody getPostById(int id)
    {
        List<CommentBody> commentBodies = new ArrayList<>();
        List<String> tags = new ArrayList<>();

        Post post = postRepository.findById(id);

        List<Tag> tagList = post.getPostTags();
        for (Tag tag : tagList)
        {
            tags.add(tag.getName());
        }

        List<PostComment> comments = post.getPostComments();
        for(PostComment comment : comments)
        {
            User commentUser = comment.getUser();
            commentBodies.add(new CommentBody(comment.getId(),
                    comment.getTime().format(formatter),
                    comment.getText(),
                    UserBody.builder().id(commentUser.getId())
                            .name(commentUser.getName())
                            .photo(commentUser.getPhoto()).build()));
        }

        return PostResponseBody.builder().id(post.getId())
                .time(post.getTime().format(formatter))
                .user(UserBody.builder().id(post.getUser().getId()).name(post.getUser().getName()).build())
                .title(post.getTitle())
                .text(post.getText())
                .likeCount(post.getVotesCount("likes"))
                .dislikeCount(post.getVotesCount("dislikes"))
                .commentCount(post.getCommentsCount())
                .viewCount(post.getViewCount())
                .comments(commentBodies)
                .tags(tags)
                .build();
    }

    @Override
    public ResponseEntity<PostWallResponseBody> getPostsForModeration(int offset, int limit, String status)
    {
        status = ModerationStatus.valueOf(status.toUpperCase()).toString();
        int count;

        if (authService.isUserAuthorize())
        {
            int userId = authService.getAuthorizedUserId();
            if (userRepository.findById(userId).getIsModerator() != 1)
                return new ResponseEntity("You're not the moderator!", HttpStatus.BAD_REQUEST);

            if (status.equals("NEW")) {
                posts = postRepository.findByModerationStatusAndIsActive(NEW, IS_ACTIVE, setPageable(offset, limit));
                count = postRepository.getPostsCountByActiveAndModStatus(IS_ACTIVE, NEW);
            }
            else {
                posts = postRepository.findByModerationStatusAndIsActiveAndModeratorId(ModerationStatus.valueOf(status),
                        IS_ACTIVE, userId,
                        setPageable(offset, limit));
                count = postRepository.getTotalPostsByModerator(IS_ACTIVE, ModerationStatus.valueOf(status), userId);
            }

            List<PostResponseBody> postsList = new ArrayList<>();
            for (Post post : posts) {
                postsList.add(PostResponseBody.builder()
                        .id(post.getId())
                        .time(post.getTime().toString())
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

        if (!authService.isUserAuthorize())
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

            int userId = authService.getAuthorizedUserId();
            User user = userRepository.findById(userId);
            if (status.equals("inactive"))
            {
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

                posts = postRepository.findByIsActiveAndModerationStatusAndUser(IS_ACTIVE,
                        ModerationStatus.valueOf(status), user, setPageable(offset, limit));
                count = postRepository.getTotalPostsCountByUser(IS_ACTIVE, ModerationStatus.valueOf(status), user);
            }
        return new ResponseEntity<>(new PostWallResponseBody(count, getListPostBodies(posts)), HttpStatus.OK);
    }

    @Override
    public ApiResponseBody postLike(int postId)
    {
        if (authService.isUserAuthorize())
        {
            User user = userRepository.findById(authService.getAuthorizedUserId());
            Post post = postRepository.findById(postId);
            PostVote postVote = postVoteRepository.findByPostAndUser(post, user);

            if (postVote != null) {
                if (postVote.getValue() == 1) {
                    return ApiResponseBody.builder().result(false).build();
                }
                else postVoteRepository.deleteById(postVote.getId());
            }
            postVoteRepository.save(PostVote.builder().user(user).post(post).time(LocalDateTime.now()).value(1).build());
        }
        return ApiResponseBody.builder().result(true).build();
    }

    @Override
    public ApiResponseBody postDislike(int postId) {
        if (authService.isUserAuthorize())
        {
            User user = userRepository.findById(authService.getAuthorizedUserId());
            Post post = postRepository.findById(postId);
            PostVote postVote = postVoteRepository.findByPostAndUser(post, user);

            if (postVote != null)
            {
                if (postVote.getValue() == 0) {
                    return ApiResponseBody.builder().result(false).build();
                }
                else postVoteRepository.deleteById(postVote.getId());
            }
            postVoteRepository.save(PostVote.builder().user(user).post(post).time(LocalDateTime.now()).value(0).build());
        }
        return ApiResponseBody.builder().result(true).build();
    }

    @Override
    public ApiResponseBody addPost(PostResponseBody post)
    {
        if (authService.isUserAuthorize()) {
            User user = userRepository.findById(authService.getAuthorizedUserId());
            if (isTitleAndTextCorrect(post))
                return errorResponse();

            else
            {
                Post newPost = postRepository.save(Post.builder().isActive(post.getActive())
                        .user(user).time(setTime(post.getTime())).text(post.getText()).title(post.getTitle())
                        .viewCount(0).moderationStatus(NEW).build());

                List<String> tags = post.getTags();
                if (!tags.isEmpty())
                    updateTagsTables(tags, newPost.getId());
            }
            return ApiResponseBody.builder().result(true).build();
        }
        return null;
    }

    @Transactional
    @Override
    public ApiResponseBody editPost(int id, PostResponseBody postResponseBody)
    {
        if (authService.isUserAuthorize())
        {
            Post post = postRepository.findById(id);
            User user = userRepository.findById(authService.getAuthorizedUserId());
            ModerationStatus status = post.getModerationStatus();

            if (user.getIsModerator() == 0 || user.getId() != post.getModeratorId())
                status = NEW;

            if (isTitleAndTextCorrect(postResponseBody))
                return errorResponse();

            else
            {
                post.setIsActive(postResponseBody.getActive());
                post.setModerationStatus(status);
                post.setTime(setTime(postResponseBody.getTime()));
                post.setTitle(postResponseBody.getTitle());
                post.setText(postResponseBody.getText());

                postRepository.save(post);

                List<String> newTags = postResponseBody.getTags();
                tagToPostRepository.deleteByPostId(id);

                if (!newTags.isEmpty())
                    updateTagsTables(newTags, id);
            }
            return ApiResponseBody.builder().result(true).build();
        }
        return null;
    }

    private void updateTagsTables (List<String> tags, int postId)
    {
        for (String tagName : tags) {
            Tag tag = tagRepository.findByName(tagName);
            if (tag == null)
                tagRepository.save(new Tag(tagName));
            int tagId = tagRepository.findByName(tagName).getId();
            tagToPostRepository.save(TagToPost.builder().postId(postId).tagId(tagId).build());
        }
    }

    private List<Post> getAndSortPosts(int offset, int limit, String mode)
    {
        int page = offset/limit;

        if (mode.equals(ModeValue.popular.toString())) {
            Pageable pageable = PageRequest.of(page, limit, Sort.by("commentsCount").descending());
            posts = postRepository.findAllPostSortedByComments(IS_ACTIVE, MODERATION_STATUS, time, pageable);
        }
        else if (mode.equals(ModeValue.best.toString())) {
            Pageable pageable = PageRequest.of(page, limit);
            posts = postRepository.findAllPostSortedByLikes(IS_ACTIVE, MODERATION_STATUS, time, pageable);
        }
        else if (mode.equals(ModeValue.recent.toString())) {
            Pageable pageable = PageRequest.of(page, limit, Sort.by("time").descending());
            posts = postRepository.findPostByIsActiveAndModerationStatusAndTimeBefore(IS_ACTIVE, MODERATION_STATUS, time, pageable);
        }
        else {
            Pageable pageable = PageRequest.of(page, limit, Sort.by("time"));
            posts = postRepository.findPostByIsActiveAndModerationStatusAndTimeBefore(IS_ACTIVE, MODERATION_STATUS, time, pageable);
        }
        return posts;
    }

    private Pageable setPageable(int offset, int limit)
    {
        int page = offset/limit;
        return PageRequest.of(page, limit);
    }
}