package com.disk.auth.controller;

import com.disk.auth.controller.request.LoginRequest;
import com.disk.auth.controller.request.RegisterRequest;
import com.disk.auth.domain.context.LoginContext;
import com.disk.auth.domain.context.RegisterContext;
import com.disk.auth.domain.service.UserService;
import com.disk.auth.service.JwtService;
import com.disk.base.response.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterRequest req) {
        RegisterContext ctx = new RegisterContext();
        ctx.setUsername(req.getUsername());
        ctx.setPassword(req.getPassword());
        ctx.setEmail(req.getEmail());
        userService.register(ctx);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody @Valid LoginRequest req) {
        LoginContext ctx = new LoginContext();
        ctx.setUsername(req.getUsername());
        ctx.setPassword(req.getPassword());
        Long userId = userService.login(ctx);
        String token = jwtService.generate(userId);
        return Result.success(Map.of("token", token, "userId", String.valueOf(userId)));
    }
}
