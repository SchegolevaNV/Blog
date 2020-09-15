package main.api.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthRequestBody
{
    @JsonProperty("e_mail")
    private String email;

    private String password;
    private String name;
    private String captcha;

    @JsonProperty("captcha_secret")
    private String captchaSecret;
}
