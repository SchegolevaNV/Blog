package main.api.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthRequestBody
{
    @JsonProperty("e_mail")
    private String email;

    private String password;
    private String name;
    private String captcha;
    private String code;

    @JsonProperty("captcha_secret")
    private String captchaSecret;
}
