package main.api.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import main.api.responses.bodies.ErrorsBody;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponseBody(Integer id,
                              Boolean result,
                              ErrorsBody errors) {
}
