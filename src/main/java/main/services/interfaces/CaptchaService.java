package main.services.interfaces;
import main.api.responses.AuthResponseBody;
import java.io.IOException;

public interface CaptchaService {
    AuthResponseBody getCaptcha() throws IOException;
}
