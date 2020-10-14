package main.services.interfaces;
import main.api.responses.AuthResponseBody;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface CaptchaService {
    ResponseEntity<AuthResponseBody> getCaptcha() throws IOException;
    boolean checkCaptchaLifetime(String code);
}
