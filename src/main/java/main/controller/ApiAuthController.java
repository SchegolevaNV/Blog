package main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/")
public class ApiAuthController {

    @GetMapping ("check")
    public ResponseEntity checkAuth()
    {
        return null;
    }
}
