package com.teacher.server.controller;

import com.teacher.common.model.ApiResponse;
import com.teacher.common.model.PageResult;
import com.teacher.pojo.dto.OrderStatusUpdateRequest;
import com.teacher.pojo.entity.OrderEntity;
import com.teacher.server.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/receive/{requirementId}")
    public ApiResponse<OrderEntity> receive(@PathVariable String requirementId) {
        return ApiResponse.ok(orderService.receiveRequirement(requirementId));
    }

    @GetMapping("/my/page")
    public ApiResponse<PageResult<OrderEntity>> myOrders(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(orderService.myOrders(pageNo, pageSize));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<OrderEntity> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ApiResponse.ok(orderService.updateStatus(id, request));
    }
}
