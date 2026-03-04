package com.teacher.server.controller;

import com.teacher.common.model.ApiResponse;
import com.teacher.common.model.PageResult;
import com.teacher.pojo.entity.DispatchRecordEntity;
import com.teacher.server.service.ExtendedBusinessService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dispatch")
public class DispatchRecordController {
    private final ExtendedBusinessService extendedBusinessService;

    public DispatchRecordController(ExtendedBusinessService extendedBusinessService) {
        this.extendedBusinessService = extendedBusinessService;
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<DispatchRecordEntity>> page(@RequestParam(defaultValue = "1") long pageNo,
                                                               @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(extendedBusinessService.dispatchPage(pageNo, pageSize));
    }
}
