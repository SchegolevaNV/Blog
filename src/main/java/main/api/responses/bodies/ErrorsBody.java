package main.api.responses.bodies;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record ErrorsBody(String title,
                         String text,
                         String code,
                         String password,
                         String captcha,
                         String email,
                         String name,
                         String photo,
                         String image) {
}
