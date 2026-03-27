package com.teacher.server.controller.admin;

import com.teacher.common.model.ApiResponse;
import com.teacher.pojo.vo.StatsOverviewVO;
import com.teacher.server.service.AdminManageService;
import com.teacher.server.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {
    private final AdminManageService adminManageService;
    private final StatsService statsService;

    public AdminStatsController(AdminManageService adminManageService, StatsService statsService) {
        this.adminManageService = adminManageService;
        this.statsService = statsService;
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Long>> overview() {
        return ApiResponse.ok(adminManageService.statistics());
    }

    @GetMapping("/business")
    public ApiResponse<Map<String, Long>> business() {
        return ApiResponse.ok(adminManageService.businessStatistics());
    }

    @GetMapping("/trend")
    public ApiResponse<StatsOverviewVO> trend(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return ApiResponse.ok(statsService.overview(startDate, endDate));
    }
}
