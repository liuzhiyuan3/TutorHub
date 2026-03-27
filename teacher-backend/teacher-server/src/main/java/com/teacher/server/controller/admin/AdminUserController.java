package com.teacher.server.controller.admin;

import com.teacher.common.model.ApiResponse;
import com.teacher.common.model.PageResult;
import com.teacher.pojo.entity.UserEntity;
import com.teacher.pojo.vo.admin.AdminUserProfileVO;
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

    @GetMapping("/{id}/profile")
    public ApiResponse<AdminUserProfileVO> profile(@PathVariable String id) {
        return ApiResponse.ok(userService.adminProfile(id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable String id, @RequestParam Integer status) {
        userService.adminUpdateStatus(id, status);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        userService.adminDelete(id);
        return ApiResponse.ok();
    }
}
