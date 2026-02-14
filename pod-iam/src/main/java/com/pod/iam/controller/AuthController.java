package com.pod.iam.controller;

import com.pod.common.core.domain.Result;
import com.pod.iam.application.AuthApplicationService;
import com.pod.iam.dto.LoginDto;
import com.pod.iam.dto.LoginResultDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/iam/auth")
public class AuthController {

    private final AuthApplicationService authService;

    public AuthController(AuthApplicationService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginResultDto> login(@RequestBody LoginDto loginDto) {
        return Result.success(authService.login(loginDto));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        // For stateless JWT, client just discards token. 
        // Server side could blacklist token here if needed.
        return Result.success();
    }
}
