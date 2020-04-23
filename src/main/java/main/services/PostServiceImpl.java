package main.services;

import lombok.Data;
import main.model.enums.ModeValue;
import main.services.bodies.PostBody;
import main.services.bodies.UserBody;
import main.api.responses.PostResponseBody;
import main.model.Post;
import main.model.enums.ModerationStatus;
import main.repositories.PostRepository;
import main.services.comparators.CommentPostComparator;
import main.services.comparators.DatePostComparator;
import main.services.comparators.LikePostComparator;
import main.services.interfaces.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Data
@Service
public class PostServiceImpl implements PostService
{
    @Autowired
    PostRepository postRepository;

    @Override
    public PostResponseBody getAllPosts(int offset, int limit, String mode)
    {
        int count = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, EEEE, HH:mm");

        Iterable<Post> postIterable = postRepository.findAll();
        List<Post> posts = new ArrayList<>();

        for (Post post : postIterable)
        {
            count++;

            if (post.getIsActive() == 1 &&
                    post.getModerationStatus().equals(ModerationStatus.ACCEPTED) &&
                    post.getTime().isBefore(LocalDateTime.now()))
            posts.add(post);
        }

        if (mode.equals(ModeValue.popular.toString()))
            posts.sort(new CommentPostComparator());
        else if (mode.equals(ModeValue.best.toString()))
            posts.sort(new LikePostComparator());
        else if (mode.equals(ModeValue.recent.toString()))
            posts.sort(new DatePostComparator());
        else
            posts.sort(new DatePostComparator().reversed());


        List<PostBody> postBodies = new ArrayList<>();
        int finish = Math.min(posts.size(), offset + limit);

        for (int i = offset; i < finish; i++)
        {
            Post post = posts.get(i);
            UserBody user = new UserBody(post.getUser().getId(), post.getUser().getName());
            String announce = "";

            if (post.getText().length() > 500) {
                announce = post.getText().substring(0, 499) + "...";
            }

            postBodies.add(PostBody.builder().id(post.getId())
                    .time(post.getTime().format(formatter))
                    .user(user)
                    .title(post.getTitle())
                    .announce(announce)
                    .likeCount(post.getVotesCount("likes"))
                    .dislikeCount(post.getVotesCount("dislikes"))
                    .commentCount(post.getCommentsCount())
                    .viewCount(post.getViewCount())
                    .build());
        }

        return new PostResponseBody(count, postBodies);
    }
}
