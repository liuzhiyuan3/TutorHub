package com.teacher.server.controller;

import com.teacher.common.model.ApiResponse;
import com.teacher.common.model.PageResult;
import com.teacher.pojo.dto.TeacherProfileRequest;
import com.teacher.pojo.entity.TeacherInfoEntity;
import com.teacher.pojo.vo.TeacherPublicDetailVO;
import com.teacher.server.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {
    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PostMapping("/profile")
    public ApiResponse<TeacherInfoEntity> saveProfile(@Valid @RequestBody TeacherProfileRequest request) {
        return ApiResponse.ok(teacherService.saveMine(request));
    }

    @GetMapping("/profile/me")
    public ApiResponse<TeacherInfoEntity> myProfile() {
        return ApiResponse.ok(teacherService.mine());
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<TeacherInfoEntity>> page(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer auditStatus) {
        return ApiResponse.ok(teacherService.page(pageNo, pageSize, keyword, auditStatus));
    }

    @GetMapping("/{id}")
    public ApiResponse<TeacherPublicDetailVO> detail(@PathVariable String id) {
        return ApiResponse.ok(teacherService.publicDetail(id));
    }

    @GetMapping("/public/{id}")
    public ApiResponse<TeacherPublicDetailVO> publicDetail(@PathVariable String id) {
        return ApiResponse.ok(teacherService.publicDetail(id));
    }
}
