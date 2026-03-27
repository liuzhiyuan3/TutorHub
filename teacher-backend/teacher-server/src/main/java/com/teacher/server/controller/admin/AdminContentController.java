package com.teacher.server.controller.admin;

import com.teacher.common.model.ApiResponse;
import com.teacher.common.model.PageResult;
import com.teacher.pojo.dto.admin.RegionSaveRequest;
import com.teacher.pojo.dto.admin.SchoolSaveRequest;
import com.teacher.pojo.dto.admin.SubjectSaveRequest;
import com.teacher.pojo.dto.admin.SubjectCategorySaveRequest;
import com.teacher.pojo.entity.RegionEntity;
import com.teacher.pojo.entity.SchoolEntity;
import com.teacher.pojo.entity.SubjectEntity;
import com.teacher.pojo.entity.SubjectCategoryEntity;
import com.teacher.server.service.ContentService;
import jakarta.validation.Valid;
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
    public ApiResponse<SubjectEntity> saveSubject(@Valid @RequestBody SubjectSaveRequest request) {
        SubjectEntity entity = new SubjectEntity();
        entity.setId(request.getId());
        entity.setSubjectName(request.getSubjectName());
        entity.setSubjectCode(request.getSubjectCode());
        entity.setSubjectCategoryId(request.getSubjectCategoryId());
        entity.setSubjectCategory(request.getSubjectCategory());
        entity.setSubjectDescription(request.getSubjectDescription());
        entity.setSubjectSort(request.getSubjectSort());
        entity.setSubjectStatus(request.getSubjectStatus());
        entity.setSubjectDeleteStatus(0);
        return ApiResponse.ok(contentService.saveSubject(entity));
    }

    @GetMapping("/subject-categories/page")
    public ApiResponse<PageResult<SubjectCategoryEntity>> subjectCategoriesPage(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(contentService.pageSubjectCategories(pageNo, pageSize));
    }

    @PostMapping("/subject-categories")
    public ApiResponse<SubjectCategoryEntity> saveSubjectCategory(@Valid @RequestBody SubjectCategorySaveRequest request) {
        SubjectCategoryEntity entity = new SubjectCategoryEntity();
        entity.setId(request.getId());
        entity.setCategoryName(request.getCategoryName());
        entity.setCategoryCode(request.getCategoryCode());
        entity.setCategorySort(request.getCategorySort());
        entity.setCategoryStatus(request.getCategoryStatus());
        entity.setCategoryDeleteStatus(0);
        return ApiResponse.ok(contentService.saveSubjectCategory(entity));
    }

    @DeleteMapping("/subject-categories/{id}")
    public ApiResponse<Void> deleteSubjectCategory(@PathVariable String id) {
        contentService.deleteSubjectCategory(id);
        return ApiResponse.ok();
    }

    @DeleteMapping("/subjects/{id}")
    public ApiResponse<Void> deleteSubject(@PathVariable String id) {
        contentService.deleteSubject(id);
        return ApiResponse.ok();
    }

    @GetMapping("/schools/page")
    public ApiResponse<PageResult<SchoolEntity>> schoolsPage(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(contentService.pageSchools(pageNo, pageSize));
    }

    @PostMapping("/schools")
    public ApiResponse<SchoolEntity> saveSchool(@Valid @RequestBody SchoolSaveRequest request) {
        SchoolEntity entity = new SchoolEntity();
        entity.setId(request.getId());
        entity.setSchoolName(request.getSchoolName());
        entity.setSchoolCode(request.getSchoolCode());
        entity.setSchoolType(request.getSchoolType());
        entity.setSchoolProvince(request.getSchoolProvince());
        entity.setSchoolCity(request.getSchoolCity());
        entity.setSchoolDistrict(request.getSchoolDistrict());
        entity.setSchoolAddress(request.getSchoolAddress());
        entity.setSchoolLongitude(request.getSchoolLongitude());
        entity.setSchoolLatitude(request.getSchoolLatitude());
        entity.setSchoolStatus(request.getSchoolStatus());
        entity.setSchoolDeleteStatus(0);
        return ApiResponse.ok(contentService.saveSchool(entity));
    }

    @DeleteMapping("/schools/{id}")
    public ApiResponse<Void> deleteSchool(@PathVariable String id) {
        contentService.deleteSchool(id);
        return ApiResponse.ok();
    }

    @GetMapping("/regions/page")
    public ApiResponse<PageResult<RegionEntity>> regionsPage(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(contentService.pageRegions(pageNo, pageSize));
    }

    @PostMapping("/regions")
    public ApiResponse<RegionEntity> saveRegion(@Valid @RequestBody RegionSaveRequest request) {
        RegionEntity entity = new RegionEntity();
        entity.setId(request.getId());
        entity.setRegionName(request.getRegionName());
        entity.setRegionCode(request.getRegionCode());
        entity.setRegionCity(request.getRegionCity());
        entity.setRegionProvince(request.getRegionProvince());
        entity.setRegionSort(request.getRegionSort());
        entity.setRegionStatus(request.getRegionStatus());
        entity.setRegionDeleteStatus(0);
        return ApiResponse.ok(contentService.saveRegion(entity));
    }

    @DeleteMapping("/regions/{id}")
    public ApiResponse<Void> deleteRegion(@PathVariable String id) {
        contentService.deleteRegion(id);
        return ApiResponse.ok();
    }
}
