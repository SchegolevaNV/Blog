package main.services.interfaces;

import main.api.responses.PostResponseBody;
import main.api.responses.PostWallResponseBody;
import main.model.Post;
import main.model.enums.ModeValue;

import main.model.enums.ModerationStatus;
import main.repositories.PostRepository;
import main.services.bodies.PostBody;
import main.services.bodies.UserBody;
import main.services.comparators.CommentPostComparator;
import main.services.comparators.DatePostComparator;
import main.services.comparators.LikePostComparator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public interface PostService
{
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, EEEE, HH:mm");

    PostWallResponseBody getAllPosts(int offset, int limit, String mode);
    PostWallResponseBody searchPosts(int offset, int limit, String query);
    PostWallResponseBody getPostsByDate(int offset, int limit, String date);
  //  PostResponseBody getPostByID(Post post);
    PostResponseBody getPostByID(int id);
    PostWallResponseBody getPostsByTag(int offset, int limit, String tag);
    PostWallResponseBody getPostsForModeration(int offset, int limit, String status);

    /** default methods*/

    default List<Post> sortPosts(List<Post> posts, String mode)
    {
            if (mode.equals(ModeValue.popular.toString()))
                posts.sort(new CommentPostComparator());
            else if (mode.equals(ModeValue.best.toString()))
                posts.sort(new LikePostComparator());
            else if (mode.equals(ModeValue.recent.toString()))
                posts.sort(new DatePostComparator());
            else
                posts.sort(new DatePostComparator().reversed());

            return posts;
    }

    default List<PostBody> getListPostBodies(List<Post> posts, int offset, int limit)
    {
        List<PostBody> postBodies = new ArrayList<>();
        int finish = Math.min(posts.size(), offset + limit);

        for (int i = offset; i < finish; i++)
        {
            Post post = posts.get(i);
            UserBody user = new UserBody(post.getUser().getId(), post.getUser().getName());

            postBodies.add(PostBody.builder().id(post.getId())
                    .time(post.getTime().format(formatter))
                    .user(user)
                    .title(post.getTitle())
                    .announce(getAnnounce(post))
                    .likeCount(post.getVotesCount("likes"))
                    .dislikeCount(post.getVotesCount("dislikes"))
                    .commentCount(post.getCommentsCount())
                    .viewCount(post.getViewCount())
                    .build());
        }
        return postBodies;
    }

    default List<Post> getPostByDefault (PostRepository postRepository)
    {
        Iterable<Post> postIterable = postRepository.findAll();
        List<Post> posts = new ArrayList<>();

        for (Post post : postIterable) {

            if (isPostAccepted(post))
                posts.add(post);
        }
        return posts;
    }

    default String getAnnounce(Post post)
    {
        String announce = "";

        if (post.getText().length() > 500) {
            announce = post.getText().substring(0, 499) + "...";
        } else announce = post.getText();

        return announce;
    }

    default boolean isPostAccepted(Post post)
    {
        return post.getIsActive() == 1 &&
                post.getModerationStatus().equals(ModerationStatus.ACCEPTED) &&
                post.getTime().isBefore(LocalDateTime.now());
    }
}
