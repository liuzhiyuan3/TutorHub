package com.teacher.server.controller;

import com.teacher.common.model.ApiResponse;
import com.teacher.pojo.entity.RegionEntity;
import com.teacher.pojo.entity.SchoolEntity;
import com.teacher.pojo.entity.SubjectEntity;
import com.teacher.server.service.ContentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/content")
public class ContentController {
    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/subjects")
    public ApiResponse<List<SubjectEntity>> subjects() {
        return ApiResponse.ok(contentService.allSubjects());
    }

    @GetMapping("/regions")
    public ApiResponse<List<RegionEntity>> regions() {
        return ApiResponse.ok(contentService.allRegions());
    }

    @GetMapping("/schools")
    public ApiResponse<List<SchoolEntity>> schools() {
        return ApiResponse.ok(contentService.allSchools());
    }
}
