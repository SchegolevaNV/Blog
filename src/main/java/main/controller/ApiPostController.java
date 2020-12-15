package main.controller;

import lombok.RequiredArgsConstructor;
import main.api.requests.ApiRequestBody;
import main.api.responses.ApiResponseBody;
import main.api.responses.PostResponseBody;
import main.api.responses.PostWallResponseBody;
import main.services.interfaces.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class ApiPostController
{
    private final PostService postService;

    @GetMapping("")
    public ResponseEntity<PostWallResponseBody> getPosts(@RequestParam(defaultValue = "0", required = false) int offset,
                                          @RequestParam(defaultValue = "10", required = false) int limit,
                                          @RequestParam(defaultValue = "recent", required = false) String mode) {
        return postService.getAllPosts(offset, limit, mode);
    }

    @GetMapping("search")
    public ResponseEntity<PostWallResponseBody> searchPosts(@RequestParam(defaultValue = "0", required = false) int offset,
                                             @RequestParam(defaultValue = "10", required = false) int limit,
                                             @RequestParam(defaultValue = "", required = false) String query) {
        return postService.searchPosts(offset, limit, query);
    }

    @GetMapping("byDate")
    public ResponseEntity<PostWallResponseBody> getPostsByDate(@RequestParam(defaultValue = "0", required = false) int offset,
                                                @RequestParam(defaultValue = "10", required = false) int limit,
                                                String date) {
        return postService.getPostsByDate(offset, limit, date);
    }

    @GetMapping("{id}")
    public ResponseEntity<PostResponseBody> getPost(@PathVariable("id") int id, Principal principal)
    {
        return postService.getPostById(id, principal);
    }

    @GetMapping("byTag")
    public ResponseEntity<PostWallResponseBody> getPostsByTag(@RequestParam(defaultValue = "0", required = false) int offset,
                                               @RequestParam(defaultValue = "10", required = false) int limit,
                                               String tag)
    {
        return postService.getPostsByTag(offset, limit, tag);
    }

    @GetMapping("moderation")
    @PreAuthorize("hasAuthority('user:moderator')")
    public ResponseEntity<PostWallResponseBody> getPostsModerationStatus(@RequestParam(defaultValue = "0", required = false)
                                                                                      int offset,
                                                                          @RequestParam(defaultValue = "10", required = false)
                                                                                  int limit,
                                                                          String  status) {
        return postService.getPostsForModeration(offset, limit, status);
    }

    @GetMapping("my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<PostWallResponseBody> getMyPosts(@RequestParam(defaultValue = "0", required = false)
                                                                        int offset,
                                                            @RequestParam(defaultValue = "10", required = false)
                                                                        int limit,
                                                            String  status)
    {
        return postService.getMyPosts(offset, limit, status);
    }

    @PostMapping("like")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ApiResponseBody> setLike(@RequestBody ApiRequestBody body)
    {
        byte postVote = 1;
        return postService.postLikeOrDislike(body.getPostId(), postVote);
    }

    @PostMapping("dislike")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ApiResponseBody> setDisLike(@RequestBody ApiRequestBody body)
    {
        byte postVote = -1;
        return postService.postLikeOrDislike(body.getPostId(), postVote);
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ApiResponseBody> addPost(@RequestBody ApiRequestBody post)
    {
        return postService.addPost(post.getTimestamp(), post.getActive(), post.getTitle(),
                post.getTags(), post.getText());
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ApiResponseBody> editPost(@PathVariable("id") int id, @RequestBody ApiRequestBody post)
    {
        return postService.editPost(id, post.getTimestamp(), post.getActive(), post.getTitle(),
            post.getTags(), post.getText());
    }
}
