package com.teacher.server.controller.admin;

import com.teacher.common.model.ApiResponse;
import com.teacher.common.model.PageResult;
import com.teacher.pojo.entity.UserEntity;
import com.teacher.server.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<UserEntity>> page(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(userService.adminPage(pageNo, pageSize, keyword));
    }
}
