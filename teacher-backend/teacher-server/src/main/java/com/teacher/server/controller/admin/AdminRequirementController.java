package com.teacher.server.controller.admin;

import com.teacher.common.model.ApiResponse;
import com.teacher.common.model.PageResult;
import com.teacher.pojo.dto.AuditRequest;
import com.teacher.pojo.entity.RequirementEntity;
import com.teacher.server.service.RequirementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/requirements")
public class AdminRequirementController {
    private final RequirementService requirementService;

    public AdminRequirementController(RequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<RequirementEntity>> page(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.ok(requirementService.adminPage(pageNo, pageSize, status));
    }

    @PutMapping("/{id}/audit")
    public ApiResponse<Void> audit(@PathVariable String id, @Valid @RequestBody AuditRequest request) {
        requirementService.adminAudit(id, request);
        return ApiResponse.ok();
    }
}
