package main.controller;

import main.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import main.configuration.Blog;
import main.configuration.Config;

@RestController
public class ApiGeneralController
{

    @Autowired
    PostRepository postRepository;

    @GetMapping("/api/init")
    public Blog getBlogInfo()
    {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
        Blog blog = context.getBean(Blog.class);
        context.close();

        return blog;
    }
//
//    @PostMapping(value = "/api/image", params = {"image"})
//    public String postImage(@RequestParam (value = "image") File image)
//    {
//        return null;
//    }
//
    @GetMapping("/api/tag")
    public ResponseEntity getTags(String query)
    {
        return null;
    }
//
//    @GetMapping(value = "/api/calendar", params = {"year"})
//    public void getCalendar(int year)
//    {
//        return;
//    }
//
//    @GetMapping("/get")
//    public @ResponseBody
//    ResponseEntity<String> get() {
//        return new ResponseEntity("GET Response", HttpStatus.OK);
//    }
}
