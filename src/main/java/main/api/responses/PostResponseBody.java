package main.api.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import main.services.bodies.CommentBody;
import main.services.bodies.UserBody;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponseBody
{
    private int id;
    private byte active;
    private String time;
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
