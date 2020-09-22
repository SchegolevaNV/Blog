package main.api.responses.bodies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentBody
{
    private int id;
    private long time;
    private String text;
    private UserBody user;
}
