package main.api.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import main.services.bodies.CommentBody;
import main.services.bodies.UserBody;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostResponseBody
{
    private int id;
    private String time;
    private UserBody user;
    private String title;
    private String announce;
    private int likeCount;
    private int dislikeCount;
    private int commentCount;
    private int viewCount;
    private List<CommentBody> comments;
    private List<String> tags;
}
