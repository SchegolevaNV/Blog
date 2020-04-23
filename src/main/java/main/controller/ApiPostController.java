package main.controller;


import main.api.responses.PostResponseBody;
import main.services.interfaces.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@ComponentScan("services")
public class ApiPostController
{
    @Autowired
    private PostService postService;


    @GetMapping("/api/post")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PostResponseBody getPosts (int offset, int limit, String mode)
    {
        return postService.getAllPosts(offset, limit, mode);
    }

//    @GetMapping(value = "/api/post/search", params = {"offset", "limit", "query"})
//    public ResponseEntity searchPosts (HttpServletRequest request,
//                                   @RequestParam(value = "offset") int offset,
//                                   @RequestParam(value = "limit") int limit,
//                                   @RequestParam(value = "query") String mode)
//    {
//        //return personWallPostService.getPersonsWallPostsByUserId(request.getSession(), id, offset, itemPerPage);
//        return null;
//    }
//
//    @GetMapping("/api/post/{id}")
//    public ResponseEntity getPost (HttpServletRequest request,
//                                      @PathVariable("id") int id)
//    {
//        //return personWallPostService.getPersonsWallPostsByUserId(request.getSession(), id, offset, itemPerPage);
//        return null;
//    }
//
//    @GetMapping(value = "/api/post/byDate", params = {"offset", "limit", "date"})
//    public ResponseEntity getPostsByDate (HttpServletRequest request,
//                                      @RequestParam(value = "offset") int offset,
//                                      @RequestParam(value = "limit") int limit,
//                                      @RequestParam(value = "date") LocalDate date)
//    {
//        //return personWallPostService.getPersonsWallPostsByUserId(request.getSession(), id, offset, itemPerPage);
//        return null;
//    }
//
//    @GetMapping(value = "/api/post/byTag", params = {"offset", "limit", "tag"})
//    public ResponseEntity getPostsByTag (HttpServletRequest request,
//                                         @RequestParam(value = "offset") int offset,
//                                         @RequestParam(value = "limit") int limit,
//                                         @RequestParam(value = "tag") String  tag)
//    {
//        //return personWallPostService.getPersonsWallPostsByUserId(request.getSession(), id, offset, itemPerPage);
//        return null;
//    }
//
//    @GetMapping(value = "/api/post/moderation", params = {"offset", "limit", "status"})
//    public ResponseEntity getPostsModerationStatus (HttpServletRequest request,
//                                        @RequestParam(value = "offset") int offset,
//                                        @RequestParam(value = "limit") int limit,
//                                        @RequestParam(value = "status") String  status)
//    {
//        //return personWallPostService.getPersonsWallPostsByUserId(request.getSession(), id, offset, itemPerPage);
//        return null;
//    }
//
//    @GetMapping(value = "/api/post/my", params = {"offset", "limit", "status"})
//    public ResponseEntity getMyPosts (HttpServletRequest request,
//                                                    @RequestParam(value = "offset") int offset,
//                                                    @RequestParam(value = "limit") int limit,
//                                                    @RequestParam(value = "status") String  status)
//    {
//        //return personWallPostService.getPersonsWallPostsByUserId(request.getSession(), id, offset, itemPerPage);
//        return null;
//    }
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
