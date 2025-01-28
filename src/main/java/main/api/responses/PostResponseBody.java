package main.api.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import main.api.responses.bodies.CommentBody;
import main.api.responses.bodies.UserBody;

import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Setter
@Getter
public class PostResponseBody {
    private int id;
    private Boolean active;
    private Long timestamp;
    private UserBody user;
    private String title;
    private String announce;
    private String text;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer commentCount;
    private Integer viewCount;
    private List<CommentBody> comments;
    private List<String> tags;
}
