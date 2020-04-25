package main.controller;

import main.repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import main.configuration.Blog;
import main.configuration.Config;

@RestController
@RequestMapping("/api/")
public class ApiGeneralController
{

    @Autowired
    PostRepository postRepository;

    @GetMapping("init")
    public Blog getBlogInfo()
    {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
        Blog blog = context.getBean(Blog.class);
        context.close();

        return blog;
    }

    @GetMapping("calendar")
    public void getCalendar(int year)
    {
        return;
    }

    @GetMapping("tag")
    public ResponseEntity getTags(String query)
    {
        return null;
    }

    @GetMapping("settings")
    public ResponseEntity getSettings()
    {
        return null;
    }

}
