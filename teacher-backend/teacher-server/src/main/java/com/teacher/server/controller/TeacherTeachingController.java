package com.teacher.server.controller;

import com.teacher.common.model.ApiResponse;
import com.teacher.pojo.entity.TeacherRegionEntity;
import com.teacher.pojo.entity.TeacherSubjectEntity;
import com.teacher.pojo.entity.TeacherSuccessRecordEntity;
import com.teacher.server.service.TeacherTeachingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/teaching")
public class TeacherTeachingController {
    private final TeacherTeachingService teacherTeachingService;

    public TeacherTeachingController(TeacherTeachingService teacherTeachingService) {
        this.teacherTeachingService = teacherTeachingService;
    }

    @PostMapping("/subject/{subjectId}")
    public ApiResponse<TeacherSubjectEntity> addSubject(@PathVariable String subjectId) {
        return ApiResponse.ok(teacherTeachingService.addSubject(subjectId));
    }

    @PostMapping("/region/{regionId}")
    public ApiResponse<TeacherRegionEntity> addRegion(@PathVariable String regionId) {
        return ApiResponse.ok(teacherTeachingService.addRegion(regionId));
    }

    @PostMapping("/success")
    public ApiResponse<TeacherSuccessRecordEntity> addSuccess(@RequestBody TeacherSuccessRecordEntity entity) {
        return ApiResponse.ok(teacherTeachingService.addSuccessRecord(entity));
    }

    @GetMapping("/subjects")
    public ApiResponse<List<TeacherSubjectEntity>> mySubjects() {
        return ApiResponse.ok(teacherTeachingService.mySubjects());
    }

    @GetMapping("/regions")
    public ApiResponse<List<TeacherRegionEntity>> myRegions() {
        return ApiResponse.ok(teacherTeachingService.myRegions());
    }

    @GetMapping("/success-records")
    public ApiResponse<List<TeacherSuccessRecordEntity>> mySuccessRecords() {
        return ApiResponse.ok(teacherTeachingService.mySuccessRecords());
    }
}
