package main.api.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import main.api.responses.bodies.UserBody;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record AuthResponseBody(Boolean result,
                               UserBody user,
                               String secret,
                               String image) {
}
