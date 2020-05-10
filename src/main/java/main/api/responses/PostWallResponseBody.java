package main.api.responses;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
public class PostWallResponseBody {
    private int count;
    private List<PostResponseBody> posts;
}
