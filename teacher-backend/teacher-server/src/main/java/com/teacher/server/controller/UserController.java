package com.teacher.server.controller;

import com.teacher.common.model.ApiResponse;
import com.teacher.pojo.dto.UserProfileCompleteRequest;
import com.teacher.pojo.dto.UserRegionUpdateRequest;
import com.teacher.pojo.dto.UserLocationUpdateRequest;
import com.teacher.pojo.entity.UserEntity;
import com.teacher.pojo.vo.UserProfileCompletenessVO;
import com.teacher.server.service.UserService;
import jakarta.validation.Valid;
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

    @PutMapping("/location")
    public ApiResponse<UserEntity> updateLocation(@Valid @RequestBody UserLocationUpdateRequest request) {
        return ApiResponse.ok(userService.updateMyLocation(request));
    }

    @PutMapping("/region")
    public ApiResponse<UserEntity> updateRegion(@RequestBody UserRegionUpdateRequest request) {
        return ApiResponse.ok(userService.updateMyRegion(request));
    }

    @GetMapping("/profile/completeness")
    public ApiResponse<UserProfileCompletenessVO> profileCompleteness() {
        return ApiResponse.ok(userService.profileCompleteness());
    }

    @PutMapping("/profile/complete")
    public ApiResponse<UserProfileCompletenessVO> completeProfile(@Valid @RequestBody UserProfileCompleteRequest request) {
        return ApiResponse.ok(userService.completeProfile(request));
    }
}
