package com.teacher.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MockPayCreateVO {
    private String orderId;
    private String orderNumber;
    private BigDecimal orderAmount;
    private String payToken;
    private LocalDateTime expireTime;
}
