package main.controller;

import lombok.RequiredArgsConstructor;
import main.api.requests.AuthRequestBody;
import main.api.responses.AuthResponseBody;
import main.services.interfaces.AuthService;
import main.services.interfaces.CaptchaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth/")
@RequiredArgsConstructor
public class ApiAuthController {

   private final AuthService authService;
   private final CaptchaService captchaService;

    @PostMapping("login")
    public ResponseEntity<AuthResponseBody> login(@RequestBody AuthRequestBody user) {
        return authService.login(user.getEmail(), user.getPassword());
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
    public ResponseEntity<AuthResponseBody> restorePassword(@RequestBody AuthRequestBody email)
    {
        return authService.restorePassword(email.getEmail());
    }

    @GetMapping("captcha")
    public ResponseEntity<AuthResponseBody> getCaptcha() throws IOException {
        return captchaService.getCaptcha();
    }

    @PostMapping("password")
    public ResponseEntity<AuthResponseBody> changePassword(@RequestBody AuthRequestBody body)
    {
        return authService.changePassword(
                body.getCode(),
                body.getPassword(),
                body.getCaptcha(),
                body.getCaptchaSecret());
    }

    @PostMapping("register")
    public ResponseEntity<AuthResponseBody> signIn(@RequestBody AuthRequestBody body)
    {
        return authService.signIn(
                body.getEmail(),
                body.getPassword(),
                body.getName(),
                body.getCaptcha(),
                body.getCaptchaSecret());
    }
}
