package com.disk.auth.controller;

import com.disk.auth.service.JwtService;
import com.disk.base.response.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Stub implementation — accepts any username/password and returns a JWT.
 * Replace with real user lookup + password verification in Phase 2.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestParam String username,
                                             @RequestParam String password) {
        // TODO Phase 2: look up user in DB, verify BCrypt password
        Long stubUserId = 1L;
        String token = jwtService.generate(stubUserId);
        return Result.success(Map.of("token", token, "userId", String.valueOf(stubUserId)));
    }
}
