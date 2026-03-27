package com.teacher.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderListItemVO {
    private String id;
    private String orderNumber;
    private String requirementId;
    private Integer orderStatus;
    private String orderStatusText;
    private Integer payStatus;
    private String payStatusText;
    private BigDecimal orderAmount;
    private BigDecimal serviceFee;
    private Integer lessonCount;
    private BigDecimal unitPrice;
    private String subjectName;
    private String counterpartyName;
    private LocalDateTime orderStartTime;
    private LocalDateTime orderEndTime;
    private String orderRemark;
    private Integer orderAuditStatus;
    private Boolean canReview;
    private Boolean canDispute;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
