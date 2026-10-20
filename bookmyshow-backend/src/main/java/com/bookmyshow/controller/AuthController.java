package com.bookmyshow.controller;

import com.bookmyshow.dto.Dto.*;
import com.bookmyshow.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public AuthRes register(@Valid @RequestBody RegisterReq req) { return authService.register(req); }

    @PostMapping("/login")
    public AuthRes login(@Valid @RequestBody LoginReq req) { return authService.login(req); }
}
