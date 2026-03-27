package com.teacher.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ParentOrderPoolItemVO {
    private String bizId;
    private String sourceType;
    private String parentOrderStage;
    private String parentOrderStageText;
    private Integer requirementStatus;
    private Integer orderStatus;
    private String statusText;
    private String orderNumber;
    private String requirementId;
    private BigDecimal orderAmount;
    private BigDecimal serviceFee;
    private Integer payStatus;
    private String payStatusText;
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
