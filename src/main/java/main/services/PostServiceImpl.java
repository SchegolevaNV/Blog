package main.services;

import main.api.responses.PostResponseBody;
import main.api.responses.PostWallResponseBody;
import main.model.Post;
import main.model.PostComment;
import main.model.Tag;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repositories.PostRepository;
import main.repositories.TagRepository;
import main.services.bodies.CommentBody;
import main.services.bodies.UserBody;
import main.services.interfaces.PostService;
import main.services.interfaces.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostServiceImpl implements PostService, QueryService
{
    @Autowired
    PostRepository postRepository;

    @Autowired
    TagRepository tagRepository;

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
                .announce(getAnnounce(post))
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

        String sessionId = AuthServiceImpl.getSession().getId();
        status = ModerationStatus.valueOf(status.toUpperCase()).toString();
        List<Post> postsList = null;
        int count = 0;
        int userId = AuthServiceImpl.activeSessions.get(sessionId);

        if (AuthServiceImpl.activeSessions.containsKey(sessionId))
        {
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
        String sessionId = AuthServiceImpl.getSession().getId();
        Query allPosts = null;
        int count = 0;

        int userId = AuthServiceImpl.activeSessions.get(sessionId);

        if (AuthServiceImpl.activeSessions.containsKey(sessionId)) {
            if (status.equals("inactive")) {
                allPosts = entityManager.createQuery("FROM Post p WHERE p.isActive = 0 AND p.user = " + userId);
                count = allPosts.getResultList().size();
                setResult(allPosts, offset, limit);
            }
            else {
                if (status.equals("pending"))
                    status = ModerationStatus.NEW.toString();
                if (status.equals("published"))
                    status = ModerationStatus.ACCEPTED.toString();
                if (status.equals("declined"))
                    status = ModerationStatus.DECLINED.toString();

                allPosts = entityManager.createQuery("FROM Post p WHERE p.isActive = 1 " +
                        "AND p.moderationStatus = '" + status + "' AND p.user = " + userId);
                count = allPosts.getResultList().size();
                setResult(allPosts, offset, limit);
            }
        }
        List<Post> posts = allPosts.getResultList();
        return new PostWallResponseBody(count, getListPostBodies(posts));
    }
}