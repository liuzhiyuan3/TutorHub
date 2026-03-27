package com.teacher.server.controller;

import com.teacher.common.model.ApiResponse;
import com.teacher.pojo.dto.MockPayCancelRequest;
import com.teacher.pojo.dto.MockPayConfirmRequest;
import com.teacher.pojo.dto.MockPayCreateRequest;
import com.teacher.pojo.vo.MockPayCreateVO;
import com.teacher.pojo.vo.MockPayResultVO;
import com.teacher.server.service.MockPayService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pay/mock")
public class MockPayController {
    private final MockPayService mockPayService;

    public MockPayController(MockPayService mockPayService) {
        this.mockPayService = mockPayService;
    }

    @PostMapping("/create")
    public ApiResponse<MockPayCreateVO> create(@Valid @RequestBody MockPayCreateRequest request) {
        return ApiResponse.ok(mockPayService.create(request.getOrderId()));
    }

    @PostMapping("/confirm")
    public ApiResponse<MockPayResultVO> confirm(@Valid @RequestBody MockPayConfirmRequest request) {
        return ApiResponse.ok(mockPayService.confirm(request.getOrderId(), request.getPayToken()));
    }

    @PostMapping("/cancel")
    public ApiResponse<MockPayResultVO> cancel(@Valid @RequestBody MockPayCancelRequest request) {
        return ApiResponse.ok(mockPayService.cancel(request.getOrderId(), request.getReason()));
    }
}
