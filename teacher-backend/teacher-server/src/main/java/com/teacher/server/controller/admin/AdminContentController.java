package com.teacher.server.controller.admin;

import com.teacher.common.model.ApiResponse;
import com.teacher.common.model.PageResult;
import com.teacher.pojo.entity.RegionEntity;
import com.teacher.pojo.entity.SchoolEntity;
import com.teacher.pojo.entity.SubjectEntity;
import com.teacher.server.service.ContentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/content")
public class AdminContentController {
    private final ContentService contentService;

    public AdminContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/subjects/page")
    public ApiResponse<PageResult<SubjectEntity>> subjectsPage(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(contentService.pageSubjects(pageNo, pageSize));
    }

    @PostMapping("/subjects")
    public ApiResponse<SubjectEntity> saveSubject(@RequestBody SubjectEntity entity) {
        return ApiResponse.ok(contentService.saveSubject(entity));
    }

    @GetMapping("/schools/page")
    public ApiResponse<PageResult<SchoolEntity>> schoolsPage(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(contentService.pageSchools(pageNo, pageSize));
    }

    @PostMapping("/schools")
    public ApiResponse<SchoolEntity> saveSchool(@RequestBody SchoolEntity entity) {
        return ApiResponse.ok(contentService.saveSchool(entity));
    }

    @GetMapping("/regions/page")
    public ApiResponse<PageResult<RegionEntity>> regionsPage(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(contentService.pageRegions(pageNo, pageSize));
    }

    @PostMapping("/regions")
    public ApiResponse<RegionEntity> saveRegion(@RequestBody RegionEntity entity) {
        return ApiResponse.ok(contentService.saveRegion(entity));
    }
}
