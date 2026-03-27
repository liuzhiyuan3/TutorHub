package com.teacher.server.controller.admin;

import com.teacher.common.model.ApiResponse;
import com.teacher.common.model.PageResult;
import com.teacher.pojo.dto.AuditRequest;
import com.teacher.pojo.entity.TeacherInfoEntity;
import com.teacher.pojo.vo.admin.AdminTeacherProfileVO;
import com.teacher.server.service.TeacherService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/teachers")
public class AdminTeacherController {
    private final TeacherService teacherService;

    public AdminTeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<TeacherInfoEntity>> page(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer auditStatus) {
        return ApiResponse.ok(teacherService.page(pageNo, pageSize, keyword, auditStatus));
    }

    @GetMapping("/{id}/profile")
    public ApiResponse<AdminTeacherProfileVO> profile(@PathVariable String id) {
        return ApiResponse.ok(teacherService.adminProfile(id));
    }

    @PutMapping("/{id}/audit")
    public ApiResponse<Void> audit(@PathVariable String id, @RequestBody AuditRequest request) {
        teacherService.audit(id, request);
        return ApiResponse.ok();
    }
}
