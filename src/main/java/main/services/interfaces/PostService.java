package main.services.interfaces;

import main.api.responses.ApiResponseBody;
import main.api.responses.PostResponseBody;
import main.api.responses.PostWallResponseBody;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;

public interface PostService
{
    ResponseEntity<PostWallResponseBody> getAllPosts(int offset, int limit, String mode);
    ResponseEntity<PostWallResponseBody> searchPosts(int offset, int limit, String query);
    ResponseEntity<PostWallResponseBody> getPostsByDate(int offset, int limit, String date);
    ResponseEntity<PostResponseBody> getPostById(int id, Principal principal);
    ResponseEntity<PostWallResponseBody> getPostsByTag(int offset, int limit, String tag);
    ResponseEntity<PostWallResponseBody> getPostsForModeration(int offset, int limit, String status);
    ResponseEntity<PostWallResponseBody> getMyPosts(int offset, int limit, String status);
    ResponseEntity<ApiResponseBody> postLikeOrDislike(int id, byte usersPostVote);
    ResponseEntity<ApiResponseBody> addPost (long timestamp, byte active, String title, List<String> tags, String text);
    ResponseEntity<ApiResponseBody> editPost (int id, long timestamp, byte active,
                                              String title, List<String> tags, String text);
}
