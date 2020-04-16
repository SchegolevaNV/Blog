package controller;

import main.model.Post;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ApiPostController
{

    @GetMapping("/api/post")
    public List<Post> getPosts(int offset, int limit, String mode)
    {
        return null;
    }
}
