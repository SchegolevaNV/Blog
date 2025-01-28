package main.controller;

import lombok.RequiredArgsConstructor;
import main.api.requests.ApiRequestBody;
import main.api.requests.AuthRequestBody;
import main.api.responses.ApiResponseBody;
import main.api.responses.AuthResponseBody;
import main.services.interfaces.AuthService;
import main.services.interfaces.CaptchaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth/")
@RequiredArgsConstructor
public class ApiAuthController {

   private final AuthService authService;
   private final CaptchaService captchaService;

    @PostMapping("login")
    public ResponseEntity<AuthResponseBody> login(HttpServletRequest req, @RequestBody AuthRequestBody user) {
        return authService.login(req, user.email(), user.password());
    }

    @GetMapping("check")
    public ResponseEntity<AuthResponseBody> checkAuth(Principal principal)
    {
        return authService.checkAuth(principal);
    }

    @GetMapping ("logout")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<AuthResponseBody> logout()
    {
        return authService.logout();
    }

    @PostMapping("restore")
    public ResponseEntity<AuthResponseBody> restorePassword(@RequestBody ApiRequestBody email, HttpServletRequest request) {
        return authService.restorePassword(email.email(), request);
    }

    @GetMapping("captcha")
    public ResponseEntity<AuthResponseBody> getCaptcha() {
        return captchaService.getCaptcha();
    }

    @PostMapping("password")
    public ResponseEntity<ApiResponseBody> changePassword(@RequestBody AuthRequestBody body)
    {
        return authService.changePassword(
                body.code(),
                body.password(),
                body.captcha(),
                body.captchaSecret());
    }

    @PostMapping("register")
    public ResponseEntity<ApiResponseBody> signIn(@RequestBody AuthRequestBody body)
    {
        return authService.signIn(
                body.email(),
                body.password(),
                body.name(),
                body.captcha(),
                body.captchaSecret());
    }
}
