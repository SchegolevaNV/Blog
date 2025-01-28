package main.api.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthRequestBody(@JsonProperty("e_mail") String email,
                              String password,
                              String name,
                              String captcha,
                              String code,
                              @JsonProperty("captcha_secret") String captchaSecret) {
}
