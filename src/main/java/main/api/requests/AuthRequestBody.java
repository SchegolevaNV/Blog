package main.api.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthRequestBody
{
    private String e_mail;
    private String password;
    private String name;
    private String captcha;
    private String captcha_secret;
}
