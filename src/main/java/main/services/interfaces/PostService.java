package main.services.interfaces;

import main.api.responses.ApiResponseBody;
import main.api.responses.PostResponseBody;
import main.api.responses.PostWallResponseBody;
import main.model.Post;
import main.api.responses.bodies.ErrorsBody;
import main.api.responses.bodies.UserBody;
import main.model.enums.Errors;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public interface PostService
{
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    PostWallResponseBody getAllPosts(int offset, int limit, String mode);
    PostWallResponseBody searchPosts(int offset, int limit, String query);
    PostWallResponseBody getPostsByDate(int offset, int limit, String date);
    PostResponseBody getPostById(int id);
    PostWallResponseBody getPostsByTag(int offset, int limit, String tag);
    ResponseEntity<PostWallResponseBody> getPostsForModeration(int offset, int limit, String status);
    ResponseEntity<PostWallResponseBody> getMyPosts(int offset, int limit, String status);
    ApiResponseBody postLike(int id);
    ApiResponseBody postDislike (int id);
    ApiResponseBody addPost (PostResponseBody post);
    ApiResponseBody editPost (int id, PostResponseBody postResponseBody);

    /** default methods*/

    default List<PostResponseBody> getListPostBodies(List<Post> posts, UtilitiesService utilitiesService)
    {

        List<PostResponseBody> postBodies = new ArrayList<>();

        for (Post post : posts) {
            LocalDateTime postTimeToUtc = utilitiesService.convertLocalTimeToUtcTime(post.getTime());
            long timestamp = utilitiesService.getTimestampFromLocalDateTime(postTimeToUtc);

            postBodies.add(PostResponseBody.builder().id(post.getId())
                    .timestamp(timestamp)
                    .user(UserBody.builder()
                            .id(post.getUser().getId())
                            .name(post.getUser().getName())
                            .build())
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

    default String getAnnounce(Post post)
    {
        String announce = "";

        if (post.getText().length() > 500) {
            announce = post.getText().substring(0, 499) + "...";
        } else announce = post.getText();

        return announce;
    }

    default boolean isTitleAndTextCorrect(PostResponseBody postResponseBody)
    {
        return postResponseBody.getTitle() == null || postResponseBody.getText() == null
            || postResponseBody.getTitle().length() < 3 || postResponseBody.getText().length() < 10;
    }

    default ApiResponseBody errorResponse ()
    {
        ErrorsBody error = ErrorsBody.builder()
                .title(Errors.TITLE_IS_NOT_SET.getTitle())
                .text(Errors.TEXT_IS_SHORT.getTitle())
                .build();

        return ApiResponseBody.builder().result(false).errors(error).build();
    }
}
