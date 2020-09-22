package main.api.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import main.api.responses.bodies.CommentBody;
import main.api.responses.bodies.UserBody;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponseBody
{
    private int id;
    private byte active;
    private Long timestamp;
    private UserBody user;
    private String title;
    private String announce;
    private String text;
    private int likeCount;
    private int dislikeCount;
    private int commentCount;
    private int viewCount;
    private List<CommentBody> comments;
    private List<String> tags;
}
