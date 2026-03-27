package com.teacher.server.controller;

import com.teacher.common.model.ApiResponse;
import com.teacher.common.model.PageResult;
import com.teacher.pojo.dto.OrderStatusUpdateRequest;
import com.teacher.pojo.entity.OrderEntity;
import com.teacher.pojo.vo.OrderListItemVO;
import com.teacher.pojo.vo.ParentOrderPoolItemVO;
import com.teacher.pojo.vo.OrderTimelineItemVO;
import com.teacher.server.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/accept/{requirementId}")
    public ApiResponse<OrderEntity> accept(@PathVariable String requirementId) {
        return ApiResponse.ok(orderService.receiveRequirement(requirementId));
    }

    @GetMapping("/my/page")
    public ApiResponse<PageResult<OrderListItemVO>> myOrders(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer payStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) java.math.BigDecimal amountMin,
            @RequestParam(required = false) java.math.BigDecimal amountMax,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime dateFrom,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime dateTo,
            @RequestParam(defaultValue = "latest") String sortBy) {
        return ApiResponse.ok(orderService.myOrders(pageNo, pageSize, status, payStatus, keyword, amountMin, amountMax, dateFrom, dateTo, sortBy));
    }

    @GetMapping("/my/pool-page")
    public ApiResponse<PageResult<ParentOrderPoolItemVO>> myOrderPool(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.ok(orderService.myOrderPoolPage(pageNo, pageSize));
    }

    @GetMapping("/{id}/timeline")
    public ApiResponse<List<OrderTimelineItemVO>> timeline(@PathVariable String id) {
        return ApiResponse.ok(orderService.orderTimeline(id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<OrderEntity> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ApiResponse.ok(orderService.updateStatus(id, request));
    }
}
