package com.teacher.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MockPayCreateRequest {
    @NotBlank(message = "订单ID不能为空")
    private String orderId;
}
