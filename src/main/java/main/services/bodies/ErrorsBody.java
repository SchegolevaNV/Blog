package main.services.bodies;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorsBody
{
    private String title;
    private String text;
    private String code;
    private String password;
    private String captcha;
    private String email;
    private String name;
    private String photo;
}
