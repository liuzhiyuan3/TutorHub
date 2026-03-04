package com.teacher.server.controller;

import com.teacher.common.model.ApiResponse;
import com.teacher.common.model.PageResult;
import com.teacher.pojo.entity.AppointmentEntity;
import com.teacher.server.service.ExtendedBusinessService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {
    private final ExtendedBusinessService extendedBusinessService;

    public AppointmentController(ExtendedBusinessService extendedBusinessService) {
        this.extendedBusinessService = extendedBusinessService;
    }

    @PostMapping
    public ApiResponse<AppointmentEntity> create(@RequestBody AppointmentEntity entity) {
        return ApiResponse.ok(extendedBusinessService.createAppointment(entity));
    }

    @GetMapping("/my/page")
    public ApiResponse<PageResult<AppointmentEntity>> myPage(@RequestParam(defaultValue = "1") long pageNo,
                                                             @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(extendedBusinessService.myAppointments(pageNo, pageSize));
    }
}
