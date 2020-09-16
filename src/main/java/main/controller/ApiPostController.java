package main.controller;

import main.api.requests.ApiRequestBody;
import main.api.responses.ApiResponseBody;
import main.api.responses.PostResponseBody;
import main.api.responses.PostWallResponseBody;
import main.services.interfaces.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@ComponentScan("services")
@RequestMapping("/api/post")
public class ApiPostController
{
    @Autowired
    private PostService postService;

    @GetMapping("")
    public PostWallResponseBody getPosts (int offset, int limit, String mode)
    {
        return postService.getAllPosts(offset, limit, mode);
    }

    @GetMapping("search")
    public PostWallResponseBody searchPosts (int offset, int limit, String query)
    {
        return postService.searchPosts(offset, limit, query);
    }

    @GetMapping("byDate")
    //@PreAuthorize("hasAuthority('user:moderate')")
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
    public ResponseEntity<PostWallResponseBody> getPostsModerationStatus (int offset, int limit, String  status)
    {
        return postService.getPostsForModeration(offset, limit, status);
    }

    @GetMapping("my")
    public ResponseEntity<PostWallResponseBody> getMyPosts (int offset, int limit, String  status)
    {
        return postService.getMyPosts(offset, limit, status);
    }

    @PostMapping("like")
    public ApiResponseBody setLike (@RequestBody ApiRequestBody body)
    {
        return postService.postLike(body.getPostId());
    }

    @PostMapping("dislike")
    public ApiResponseBody setDisLike (@RequestBody ApiRequestBody body)
    {
        return postService.postDislike(body.getPostId());
    }

    @PostMapping("")
    public ApiResponseBody addPost(@RequestBody PostResponseBody post)
    {
        return postService.addPost(post);
    }

    @PutMapping("{id}")
    public ApiResponseBody editPost (@PathVariable("id") int id, @RequestBody PostResponseBody post) {return postService.editPost(id, post);}
}
