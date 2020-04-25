package main.services;

import lombok.Data;
import main.api.responses.PostResponseBody;
import main.api.responses.PostWallResponseBody;
import main.model.Post;
import main.model.PostComment;
import main.model.Tag;
import main.model.User;
import main.repositories.PostRepository;
import main.services.bodies.CommentBody;
import main.services.bodies.UserBody;
import main.services.bodies.UserCommentBody;
import main.services.interfaces.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Data
@Service
public class PostServiceImpl implements PostService
{
    @Autowired
    PostRepository postRepository;

    @Override
    public PostWallResponseBody getAllPosts (int offset, int limit, String mode)
    {
        int count = 0;
        List<Post> posts = new ArrayList<>();
        Iterable<Post> postIterable = postRepository.findAll();

        for (Post post : postIterable) {
            count++;

            if (isPostAccepted(post))
                posts.add(post);
        }

        posts = sortPosts(posts, mode);

        return new PostWallResponseBody(count, getListPostBodies(posts, offset, limit));
    }

    @Override
    public PostWallResponseBody searchPosts(int offset, int limit, String query)
    {
        Iterable<Post> postIterable = postRepository.findAll();

        int count = 0;
        List<Post> posts = new ArrayList<>();
        for (Post post : postIterable) {
            count++;

            if (isPostAccepted(post))
            {
                if (query == null || post.getText().contains(query))
                {
                    posts.add(post);;
                }
            }
        }

        return new PostWallResponseBody(count, getListPostBodies(posts, offset, limit));
    }

    @Override
    public PostWallResponseBody getPostsByDate(int offset, int limit, String date) {

        Iterable<Post> postIterable = postRepository.findAll();
        LocalDate localDate = LocalDate.parse(date);

        int count = 0;
        List<Post> posts = new ArrayList<>();
        for (Post post : postIterable) {
            count++;

            if (isPostAccepted(post) && post.getTime().toLocalDate().isEqual(localDate))
            {
                posts.add(post);
            }
        }

        return new PostWallResponseBody(count, getListPostBodies(posts, offset, limit));
    }

    @Override
    public PostResponseBody getPostByID(Post post)
    {
        List<CommentBody> commentBodies = new ArrayList<>();
        List<String> tags = new ArrayList<>();

        UserBody postUser = new UserBody(post.getUser().getId(), post.getUser().getName());

        List<Tag> tagList = post.getPostTags();
        for (Tag tag : tagList)
        {
            tags.add(tag.getName());
        }

        List<PostComment> comments = post.getPostComments();
        for(PostComment comment : comments)
        {
            User commentUser = comment.getUser();
            UserCommentBody userCommentBody = new UserCommentBody(commentUser.getId(),
                                                                  commentUser.getName(),
                                                                  commentUser.getPhoto());

            commentBodies.add(new CommentBody(comment.getId(),
                                              comment.getTime().format(formatter),
                                              comment.getText(),
                                              userCommentBody));
        }

        return PostResponseBody.builder().id(post.getId())
                .time(post.getTime().format(formatter))
                .user(postUser)
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
    public PostWallResponseBody getPostsByTag(int offset, int limit, String tag) {
        Iterable<Post> postIterable = postRepository.findAll();

        int count = 0;
        List<Post> posts = new ArrayList<>();
        for (Post post : postIterable) {
            count++;

            if (isPostAccepted(post))
            {
                List<Tag> tagList = post.getPostTags();
                for (Tag tags : tagList)
                {
                    if (tags.getName().equals(tag))
                        posts.add(post);
                }
            }
        }
        return new PostWallResponseBody(count, getListPostBodies(posts, offset, limit));
    }

    @Override
    public PostWallResponseBody getPostsForModeration(int offset, int limit, String status) {
        return null;
    }
}