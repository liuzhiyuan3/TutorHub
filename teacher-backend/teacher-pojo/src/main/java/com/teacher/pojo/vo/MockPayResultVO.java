package com.teacher.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MockPayResultVO {
    private String orderId;
    private Integer orderStatus;
    private String orderStatusText;
    private Integer payStatus;
    private String payStatusText;
    private String message;
}
