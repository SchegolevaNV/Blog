package main.services.interfaces;
import main.api.responses.AuthResponseBody;
import org.springframework.http.ResponseEntity;

public interface CaptchaService {
    ResponseEntity<AuthResponseBody> getCaptcha();
    boolean checkCaptchaLifetime(String code);
}
