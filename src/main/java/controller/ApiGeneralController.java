package controller;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import services.Blog;
import services.BlogConfig;

@RestController
public class ApiGeneralController
{
    @GetMapping("/api/init")
    public ResponseEntity getBlogInfo()
    {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BlogConfig.class);
        Blog blog = context.getBean(Blog.class);

        return new ResponseEntity(blog, HttpStatus.OK);
    }
}
