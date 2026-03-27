package com.teacher.server.controller;

import com.teacher.common.model.ApiResponse;
import com.teacher.pojo.dto.LoginRequest;
import com.teacher.pojo.dto.RegisterRequest;
import com.teacher.pojo.dto.WechatLoginRequest;
import com.teacher.pojo.dto.WechatPhoneLoginRequest;
import com.teacher.pojo.vo.LoginVO;
import com.teacher.pojo.vo.admin.AdminSessionVO;
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

    @GetMapping("/admin/me")
    public ApiResponse<AdminSessionVO> adminMe() {
        return ApiResponse.ok(authService.adminMe());
    }

    @PostMapping("/register")
    public ApiResponse<LoginVO> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @PostMapping("/user/login/wechat")
    public ApiResponse<LoginVO> userWechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        return ApiResponse.ok(authService.userWechatLogin(request));
    }

    @PostMapping("/user/login/wechat-phone")
    public ApiResponse<LoginVO> userWechatPhoneLogin(@Valid @RequestBody WechatPhoneLoginRequest request) {
        return ApiResponse.ok(authService.userWechatPhoneLogin(request));
    }
}
