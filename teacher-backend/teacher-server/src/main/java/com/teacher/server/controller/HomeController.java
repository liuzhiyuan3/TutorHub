package com.teacher.server.controller;

import com.teacher.common.model.ApiResponse;
import com.teacher.common.model.PageResult;
import com.teacher.pojo.vo.FilterMetaVO;
import com.teacher.pojo.vo.HomeOverviewVO;
import com.teacher.pojo.vo.PublicRequirementListItemVO;
import com.teacher.pojo.vo.PublicTeacherListItemVO;
import com.teacher.server.service.HomeQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/home")
public class HomeController {
    private final HomeQueryService homeQueryService;

    public HomeController(HomeQueryService homeQueryService) {
        this.homeQueryService = homeQueryService;
    }

    @GetMapping("/overview")
    public ApiResponse<HomeOverviewVO> overview() {
        return ApiResponse.ok(homeQueryService.overview());
    }

    @GetMapping("/filters")
    public ApiResponse<FilterMetaVO> filters() {
        return ApiResponse.ok(homeQueryService.filterMeta());
    }

    @GetMapping("/teachers/search")
    public ApiResponse<PageResult<PublicTeacherListItemVO>> teacherSearch(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String subjectId,
            @RequestParam(required = false) String regionId,
            @RequestParam(required = false) Integer tutoringMethod,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String schoolKeyword,
            @RequestParam(required = false) Integer minTeachingYears,
            @RequestParam(required = false) Integer maxTeachingYears,
            @RequestParam(required = false) BigDecimal userLat,
            @RequestParam(required = false) BigDecimal userLng,
            @RequestParam(required = false) BigDecimal maxDistanceKm,
            @RequestParam(defaultValue = "hot") String sortBy) {
        return ApiResponse.ok(homeQueryService.teacherSearch(
                pageNo, pageSize, subjectId, regionId, tutoringMethod, auditStatus, keyword,
                schoolKeyword, minTeachingYears, maxTeachingYears, userLat, userLng, maxDistanceKm, sortBy));
    }

    @GetMapping("/requirements/search")
    public ApiResponse<PageResult<PublicRequirementListItemVO>> requirementSearch(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String subjectId,
            @RequestParam(required = false) String regionId,
            @RequestParam(required = false) Integer tutoringMethod,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary,
            @RequestParam(required = false) BigDecimal budgetMin,
            @RequestParam(required = false) BigDecimal budgetMax,
            @RequestParam(required = false) Integer urgency,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTimeFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTimeTo,
            @RequestParam(required = false) String gradeKeyword,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sortBy) {
        return ApiResponse.ok(homeQueryService.requirementSearch(
                pageNo, pageSize, subjectId, regionId, tutoringMethod, minSalary, maxSalary,
                budgetMin, budgetMax, urgency, startTimeFrom, startTimeTo, gradeKeyword, keyword, sortBy));
    }
}
