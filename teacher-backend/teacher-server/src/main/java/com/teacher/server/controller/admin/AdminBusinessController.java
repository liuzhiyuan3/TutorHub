package com.teacher.server.controller.admin;

import com.teacher.common.model.ApiResponse;
import com.teacher.common.model.PageResult;
import com.teacher.pojo.entity.AppointmentEntity;
import com.teacher.pojo.entity.DispatchRecordEntity;
import com.teacher.pojo.entity.FavoriteTeacherEntity;
import com.teacher.server.service.ExtendedBusinessService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/business")
public class AdminBusinessController {
    private final ExtendedBusinessService extendedBusinessService;

    public AdminBusinessController(ExtendedBusinessService extendedBusinessService) {
        this.extendedBusinessService = extendedBusinessService;
    }

    @GetMapping("/appointments/page")
    public ApiResponse<PageResult<AppointmentEntity>> appointmentPage(@RequestParam(defaultValue = "1") long pageNo,
                                                                      @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(extendedBusinessService.adminAppointmentPage(pageNo, pageSize));
    }

    @GetMapping("/favorites/page")
    public ApiResponse<PageResult<FavoriteTeacherEntity>> favoritePage(@RequestParam(defaultValue = "1") long pageNo,
                                                                       @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(extendedBusinessService.adminFavoritePage(pageNo, pageSize));
    }

    @GetMapping("/dispatch/page")
    public ApiResponse<PageResult<DispatchRecordEntity>> dispatchPage(@RequestParam(defaultValue = "1") long pageNo,
                                                                      @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(extendedBusinessService.dispatchPage(pageNo, pageSize));
    }
}
