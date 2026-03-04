package com.teacher.server.controller;

import com.teacher.common.model.ApiResponse;
import com.teacher.pojo.entity.UserEntity;
import com.teacher.server.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserEntity> me() {
        return ApiResponse.ok(userService.me());
    }

    @PutMapping("/me")
    public ApiResponse<UserEntity> updateMe(@RequestBody UserEntity request) {
        return ApiResponse.ok(userService.updateMe(request));
    }
}
