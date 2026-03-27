package com.teacher.server.controller;

import com.teacher.common.model.ApiResponse;
import com.teacher.pojo.vo.PublicAdvertisingVO;
import com.teacher.pojo.vo.PublicOptionVO;
import com.teacher.pojo.vo.PublicSlideVO;
import com.teacher.pojo.vo.SubjectCategoryTreeVO;
import com.teacher.server.service.ContentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ApiResponse<List<PublicOptionVO>> subjects() {
        return ApiResponse.ok(contentService.publicSubjects());
    }

    @GetMapping("/subject-categories")
    public ApiResponse<List<SubjectCategoryTreeVO>> subjectCategories() {
        return ApiResponse.ok(contentService.publicSubjectCategoryTree());
    }

    @GetMapping("/regions")
    public ApiResponse<List<PublicOptionVO>> regions() {
        return ApiResponse.ok(contentService.publicRegions());
    }

    @GetMapping("/schools")
    public ApiResponse<List<PublicOptionVO>> schools() {
        return ApiResponse.ok(contentService.publicSchools());
    }

    @GetMapping("/slides")
    public ApiResponse<List<PublicSlideVO>> slides(@RequestParam(required = false) Integer module) {
        return ApiResponse.ok(contentService.publicSlides(module));
    }

    @GetMapping("/advertising")
    public ApiResponse<List<PublicAdvertisingVO>> advertising(@RequestParam(required = false) String source) {
        return ApiResponse.ok(contentService.publicAdvertising(source));
    }
}
