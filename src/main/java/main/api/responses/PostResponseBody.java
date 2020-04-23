package main.api.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import main.services.bodies.PostBody;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class PostResponseBody {
    private int count;
    private List<PostBody> posts;
}
