package main.controller;


import main.api.responses.PostResponseBody;
import main.api.responses.PostWallResponseBody;
import main.repositories.PostRepository;
import main.services.interfaces.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@ComponentScan("services")
@RequestMapping("/api/post")
public class ApiPostController
{
    @Autowired
    private PostService postService;

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
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

    @GetMapping(value = "moderation")
    public PostWallResponseBody getPostsModerationStatus (int offset, int limit, String  status)
    {
        return postService.getPostsForModeration(offset, limit, status);
    }

    @GetMapping("my")
    public PostWallResponseBody getMyPosts (int offset, int limit, String  status)
    {
        return postService.getMyPosts(offset, limit, status);
    }
//
//    @PostMapping(value = "/api/post", params = {"time", "active", "title", "text", "tags"})
//    public void setPost(LocalDateTime time, byte active, String title, String text, String tags)
//    {
//       return;
//    }
//
//    @PutMapping(value = "/api/post/{id}", params = {"time", "active", "title", "text", "tags"})
//    public void postEdit(@PathVariable("id") int id, LocalDateTime time, byte active, String title, String text, String tags)
//    {
//        return;
//    }
//
//    @PostMapping(value = "/api/comment", params = {"parent_id", "post_id", "text"})
//    public void addComment(int parent_id, int post_id, String text)
//    {
//        return;
//    }
//
//    @PostMapping(value = "/api/moderation", params = {"post_id", "decision"})
//    public void moderatePost()
//    {
//       // ???
//        return;
//    }

}
