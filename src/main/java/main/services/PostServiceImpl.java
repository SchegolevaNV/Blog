package main.services;

import javassist.expr.NewArray;
import liquibase.pro.packaged.N;
import main.api.responses.ApiResponseBody;
import main.api.responses.PostResponseBody;
import main.api.responses.PostWallResponseBody;
import main.model.*;
import main.model.enums.ModerationStatus;
import main.repositories.*;
import main.services.bodies.CommentBody;
import main.services.bodies.ErrorsBody;
import main.services.bodies.UserBody;
import main.services.interfaces.AuthService;
import main.services.interfaces.PostService;
import main.services.interfaces.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static main.model.enums.ModerationStatus.NEW;

@Service
public class PostServiceImpl implements PostService, QueryService
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

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public PostWallResponseBody getAllPosts (int offset, int limit, String mode)
    {
        Query allPosts = entityManager.createQuery(postsSelect).setParameter("dateNow", LocalDateTime.now());

        int count = allPosts.getResultList().size();

        allPosts.setFirstResult(offset);
        allPosts.setMaxResults(limit);

        List<Post> posts = sortPosts(allPosts.getResultList(), mode);

        return new PostWallResponseBody(count, getListPostBodies(posts));
    }

    @Override
    public PostWallResponseBody searchPosts(int offset, int limit, String query)
    {
        Query allPosts = entityManager.createQuery(postsSelect.concat(" AND p.text LIKE '%" + query + "%'"))
                .setParameter("dateNow", LocalDateTime.now());

        int count = allPosts.getResultList().size();

        allPosts.setFirstResult(offset);
        allPosts.setMaxResults(limit);

        List<Post> posts = allPosts.getResultList();

        return new PostWallResponseBody(count, getListPostBodies(posts));
    }

    @Override
    public PostWallResponseBody getPostsByDate(int offset, int limit, String date) {

        Query allPosts = entityManager.createQuery(postsSelect.concat(" AND to_char(p.time,'YYYY-MM-DD') LIKE '%" + date + "%'"))
                .setParameter("dateNow", LocalDateTime.now());

        int count = allPosts.getResultList().size();

        allPosts.setFirstResult(offset);
        allPosts.setMaxResults(limit);

        List<Post> posts = allPosts.getResultList();

        return new PostWallResponseBody(count, getListPostBodies(posts));
    }

    @Override
    public PostWallResponseBody getPostsByTag(int offset, int limit, String tag)
    {
        Tag myTag = tagRepository.findByName(tag);
        List<Post> posts = myTag.getTagsPosts();

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
    public PostWallResponseBody getPostsForModeration(int offset, int limit, String status) {

        status = ModerationStatus.valueOf(status.toUpperCase()).toString();
        List<Post> postsList = null;
        int count = 0;

        if (authService.isUserAuthorize())
        {
            int userId = authService.getAuthorizedUserId();
            Query allPosts = entityManager.createQuery("FROM Post p WHERE p.isActive = 1 " +
                    "AND p.moderationStatus = '" + status + "' AND p.moderatorId = " + userId);
            count = allPosts.getResultList().size();
            setResult(allPosts, offset, limit);

            postsList = allPosts.getResultList();
        }

        List<PostResponseBody> posts = new ArrayList<>();
        for (Post post : postsList)
        {
            posts.add(PostResponseBody.builder()
                    .id(post.getId())
                    .time(post.getTime().toString())
                    .user(UserBody.builder().id(post.getUser().getId()).name(post.getUser().getName()).build())
                    .title(post.getTitle())
                    .announce(getAnnounce(post)).build());
        }

        return new PostWallResponseBody(count, posts);
    }

    @Override
    public PostWallResponseBody getMyPosts(int offset, int limit, String status)
    {
        Query allPosts = null;
        int count = 0;

        if (authService.isUserAuthorize()) {
            int userId = authService.getAuthorizedUserId();
            if (status.equals("inactive")) {
                allPosts = entityManager.createQuery("FROM Post p WHERE p.isActive = 0 AND p.user = " + userId);
            }
            else {
                if (status.equals("pending"))
                    status = NEW.toString();
                if (status.equals("published"))
                    status = ModerationStatus.ACCEPTED.toString();
                if (status.equals("declined"))
                    status = ModerationStatus.DECLINED.toString();

                allPosts = entityManager.createQuery("FROM Post p WHERE p.isActive = 1 " +
                        "AND p.moderationStatus = '" + status + "' AND p.user = " + userId);
            }
            count = allPosts.getResultList().size();
            setResult(allPosts, offset, limit);
        }
        List<Post> posts = allPosts.getResultList();
        return new PostWallResponseBody(count, getListPostBodies(posts));
    }

    @Override
    public ApiResponseBody postLike(int postId)
    {
        if (authService.isUserAuthorize())
        {
            User user = userRepository.findById(authService.getAuthorizedUserId());
            Post post = postRepository.findById(postId);
            PostVote postVote = postVoteRepository.findByPostAndUser(post, user);

            if (postVote != null)
            {
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
            postVoteRepository.save(PostVote.builder().user(user).post(post).time(LocalDateTime.now()).value(1).build());
        }
        return ApiResponseBody.builder().result(true).build();
    }

    @Transactional
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
                if (tags.size() > 0)
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

            else {
                Query updatePost = entityManager.createQuery("UPDATE Post SET isActive = :is_active, " +
                        "moderationStatus = :status, " + "time = :time, " + "title = :title, " + "text = :text " +
                        "WHERE id = " + id);

                updatePost.setParameter("is_active", postResponseBody.getActive());
                updatePost.setParameter("status", status);
                updatePost.setParameter("time", setTime(postResponseBody.getTime()));
                updatePost.setParameter("title", postResponseBody.getTitle());
                updatePost.setParameter("text", postResponseBody.getText());
                updatePost.executeUpdate();

                List<String> newTags = postResponseBody.getTags();
                tagToPostRepository.deleteByPostId(id);

                if (newTags.size() > 0)
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
}