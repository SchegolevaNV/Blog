package main.services.bodies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentBody
{
    private int id;
    private String time;
    private String text;
    private UserCommentBody user;
}
