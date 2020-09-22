package main.controller;

import main.api.requests.ApiRequestBody;
import main.api.responses.ApiResponseBody;
import main.api.responses.PostResponseBody;
import main.api.responses.PostWallResponseBody;
import main.services.interfaces.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post")
public class ApiPostController
{
    @Autowired
    private PostService postService;

    @GetMapping("")
    public PostWallResponseBody getPosts (@RequestParam(defaultValue = "0", required = false) int offset,
                                          @RequestParam(defaultValue = "20", required = false) int limit,
                                          @RequestParam(defaultValue = "recent", required = false) String mode)
    {
        return postService.getAllPosts(offset, limit, mode);
    }

    @GetMapping("search")
    public PostWallResponseBody searchPosts (int offset, int limit, String query)
    {
        return postService.searchPosts(offset, limit, query);
    }

    @GetMapping("byDate")
    public PostWallResponseBody getPostsByDate (int offset, int limit, String date)
    {
        return postService.getPostsByDate(offset, limit, date);
    }

    @GetMapping("{id}")
    public PostResponseBody getPost (@PathVariable("id") int id)
    {
        return postService.getPostById(id);
    }

    @GetMapping("byTag")
    public PostWallResponseBody getPostsByTag (int offset, int limit, String tag)
    {
        return postService.getPostsByTag(offset, limit, tag);
    }

    @GetMapping("moderation")
    @PreAuthorize("hasAuthority('user:moderator')")
    public ResponseEntity<PostWallResponseBody> getPostsModerationStatus (int offset, int limit, String  status)
    {
        return postService.getPostsForModeration(offset, limit, status);
    }

    @GetMapping("my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<PostWallResponseBody> getMyPosts (int offset, int limit, String  status)
    {
        return postService.getMyPosts(offset, limit, status);
    }

    @PostMapping("like")
    @PreAuthorize("hasAuthority('user:write')")
    public ApiResponseBody setLike (@RequestBody ApiRequestBody body)
    {
        return postService.postLike(body.getPostId());
    }

    @PostMapping("dislike")
    @PreAuthorize("hasAuthority('user:write')")
    public ApiResponseBody setDisLike (@RequestBody ApiRequestBody body)
    {
        return postService.postDislike(body.getPostId());
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('user:write')")
    public ApiResponseBody addPost(@RequestBody PostResponseBody post)
    {
        return postService.addPost(post);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAuthority('user:write')")
    public ApiResponseBody editPost (@PathVariable("id") int id, @RequestBody PostResponseBody post)
    {return postService.editPost(id, post);}
}
