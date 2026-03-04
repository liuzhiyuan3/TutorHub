package com.teacher.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    @NotNull(message = "订单状态不能为空")
    private Integer orderStatus;
    private String orderRemark;
}
