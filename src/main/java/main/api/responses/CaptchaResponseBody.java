package main.api.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CaptchaResponseBody
{
    String secret;
    String image;
}
