package main.services;

import lombok.Data;
import main.services.bodies.PostBody;
import main.services.bodies.UserBody;
import main.api.responses.PostResponseBody;
import main.model.Post;
import main.repositories.PostRepository;
import main.services.interfaces.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@Service
public class PostServiceImpl implements PostService
{
    @Autowired
    PostRepository postRepository;

    private List<Post> posts = new ArrayList<>();
    private int count = 0;

    @Override
    public PostResponseBody getAllPosts (int offset, int limit, String mode)
    {
        HashMap<List<Post>, Integer> postsHash = getPostListAndCount(postRepository);

        for (Map.Entry<List<Post>, Integer> map : postsHash.entrySet()) {
            posts = map.getKey();
            count = map.getValue();
        }

        sortPosts(posts, mode);

        return new PostResponseBody(count, getListPostBodies(posts, offset, limit));
    }

    @Override
    public PostResponseBody searchPosts(int offset, int limit, String query) {
        return null;
    }
}
