package main.services.interfaces;

import main.api.responses.ApiResponseBody;
import main.api.responses.PostResponseBody;
import main.api.responses.PostWallResponseBody;
import org.springframework.http.ResponseEntity;

public interface PostService
{
    ResponseEntity<PostWallResponseBody> getAllPosts(int offset, int limit, String mode);
    ResponseEntity<PostWallResponseBody> searchPosts(int offset, int limit, String query);
    ResponseEntity<PostWallResponseBody> getPostsByDate(int offset, int limit, String date);
    ResponseEntity<PostResponseBody> getPostById(int id);
    ResponseEntity<PostWallResponseBody> getPostsByTag(int offset, int limit, String tag);
    ResponseEntity<PostWallResponseBody> getPostsForModeration(int offset, int limit, String status);
    ResponseEntity<PostWallResponseBody> getMyPosts(int offset, int limit, String status);
    ResponseEntity<ApiResponseBody> postLike(int id);
    ResponseEntity<ApiResponseBody> postDislike (int id);
    ResponseEntity<ApiResponseBody> addPost (PostResponseBody post);
    ResponseEntity<ApiResponseBody> editPost (int id, PostResponseBody postResponseBody);
}
