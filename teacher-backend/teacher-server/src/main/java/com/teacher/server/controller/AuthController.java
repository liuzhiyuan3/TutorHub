package com.teacher.server.controller;

import com.teacher.common.model.ApiResponse;
import com.teacher.pojo.dto.LoginRequest;
import com.teacher.pojo.dto.RegisterRequest;
import com.teacher.pojo.vo.LoginVO;
import com.teacher.server.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/user/login")
    public ApiResponse<LoginVO> userLogin(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.userLogin(request));
    }

    @PostMapping("/admin/login")
    public ApiResponse<LoginVO> adminLogin(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.adminLogin(request));
    }

    @PostMapping("/register")
    public ApiResponse<LoginVO> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }
}
