package main.api.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import main.services.bodies.UserBody;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class AuthResponseBody
{
    private Boolean result;
    private UserBody user;
    private String secret;
    private String image;
}
