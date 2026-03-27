package com.teacher.server.controller.admin;

import com.teacher.common.model.ApiResponse;
import com.teacher.common.model.PageResult;
import com.teacher.pojo.dto.AuditRequest;
import com.teacher.pojo.dto.OrderStatusUpdateRequest;
import com.teacher.pojo.entity.OrderEntity;
import com.teacher.server.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<OrderEntity>> page(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status) {
        return ApiResponse.ok(orderService.adminPage(pageNo, pageSize, status));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<OrderEntity> updateStatus(@PathVariable String id, @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ApiResponse.ok(orderService.updateStatus(id, request));
    }

    @PutMapping("/{id}/audit")
    public ApiResponse<Void> audit(@PathVariable String id, @Valid @RequestBody AuditRequest request) {
        orderService.adminAudit(id, request);
        return ApiResponse.ok();
    }
}
