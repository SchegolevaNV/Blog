package main.services.bodies;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class PostBody
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
}
