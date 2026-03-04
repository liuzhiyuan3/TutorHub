package com.teacher.server.controller.admin;

import com.teacher.common.model.ApiResponse;
import com.teacher.server.service.AdminManageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {
    private final AdminManageService adminManageService;

    public AdminStatsController(AdminManageService adminManageService) {
        this.adminManageService = adminManageService;
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Long>> overview() {
        return ApiResponse.ok(adminManageService.statistics());
    }
}
