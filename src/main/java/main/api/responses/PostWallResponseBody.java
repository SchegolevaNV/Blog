package main.api.responses;

import lombok.*;
import main.services.bodies.PostBody;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostWallResponseBody {
    private int count;
    private List<PostBody> posts;
}
