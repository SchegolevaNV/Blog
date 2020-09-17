package main.controller;

import main.api.requests.AuthRequestBody;
import main.api.responses.AuthResponseBody;
import main.services.interfaces.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth/")
public class ApiAuthController {

    @Autowired
    AuthService authService;

    @PostMapping("login")
    public AuthResponseBody login(@RequestBody AuthRequestBody user)
    {
        return authService.login(user.getEmail(), user.getPassword());
    }

    @GetMapping("check")
    public AuthResponseBody checkAuth(Principal principal)
    {
        return authService.checkAuth(principal);
    }

    @GetMapping ("logout")
    public AuthResponseBody logout()
    {
        return authService.logout();
    }

    @PostMapping("restore")
    public AuthResponseBody restorePassword(@RequestBody AuthRequestBody email)
    {
        return authService.restorePassword(email.getEmail());
    }

    @GetMapping("captcha")
    public AuthResponseBody getCaptcha() throws IOException {
        return authService.getCaptcha();
    }

    @PostMapping("password")
    public AuthResponseBody changePassword(@RequestBody AuthRequestBody body)
    {
        return authService.changePassword(body);
    }

    @PostMapping("register")
    public AuthResponseBody signIn(@RequestBody AuthRequestBody body)
    {
        return authService.signIn(body);
    }
}
