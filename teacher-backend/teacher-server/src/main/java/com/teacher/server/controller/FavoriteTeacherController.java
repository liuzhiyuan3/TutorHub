package com.teacher.server.controller;

import com.teacher.common.model.ApiResponse;
import com.teacher.common.model.PageResult;
import com.teacher.pojo.entity.FavoriteTeacherEntity;
import com.teacher.server.service.ExtendedBusinessService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteTeacherController {
    private final ExtendedBusinessService extendedBusinessService;

    public FavoriteTeacherController(ExtendedBusinessService extendedBusinessService) {
        this.extendedBusinessService = extendedBusinessService;
    }

    @PostMapping("/{teacherId}")
    public ApiResponse<FavoriteTeacherEntity> add(@PathVariable String teacherId) {
        return ApiResponse.ok(extendedBusinessService.addFavorite(teacherId));
    }

    @GetMapping("/my/page")
    public ApiResponse<PageResult<FavoriteTeacherEntity>> myPage(@RequestParam(defaultValue = "1") long pageNo,
                                                                 @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(extendedBusinessService.myFavorites(pageNo, pageSize));
    }
}
