package main.api.responses.bodies;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record UserBody(int id,
                       String name,
                       String photo,
                       String email,
                       Boolean moderation,
                       Integer moderationCount,
                       Boolean settings) {

}
