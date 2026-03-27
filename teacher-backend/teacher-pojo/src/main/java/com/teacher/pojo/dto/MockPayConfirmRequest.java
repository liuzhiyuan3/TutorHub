package com.teacher.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MockPayConfirmRequest {
    @NotBlank(message = "订单ID不能为空")
    private String orderId;

    @NotBlank(message = "支付令牌不能为空")
    private String payToken;
}
