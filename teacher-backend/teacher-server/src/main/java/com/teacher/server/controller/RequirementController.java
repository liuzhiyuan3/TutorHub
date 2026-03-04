package com.teacher.server.controller;

import com.teacher.common.model.ApiResponse;
import com.teacher.common.model.PageResult;
import com.teacher.pojo.dto.RequirementCreateRequest;
import com.teacher.pojo.entity.RequirementEntity;
import com.teacher.server.service.RequirementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requirement")
public class RequirementController {
    private final RequirementService requirementService;

    public RequirementController(RequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @PostMapping
    public ApiResponse<RequirementEntity> create(@Valid @RequestBody RequirementCreateRequest request) {
        return ApiResponse.ok(requirementService.create(request));
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<RequirementEntity>> page(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String subjectId,
            @RequestParam(required = false) String regionId) {
        return ApiResponse.ok(requirementService.pageOpen(pageNo, pageSize, subjectId, regionId));
    }

    @GetMapping("/my/page")
    public ApiResponse<PageResult<RequirementEntity>> myPage(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(requirementService.myPage(pageNo, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<RequirementEntity> detail(@PathVariable String id) {
        return ApiResponse.ok(requirementService.detail(id));
    }
}
