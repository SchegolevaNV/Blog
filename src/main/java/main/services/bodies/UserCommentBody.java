package main.services.bodies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCommentBody
{
    private int id;
    private String name;
    private String photo;
}
