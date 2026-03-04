package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class OrderEntity {
    @TableId
    private String id;
    private String orderNumber;
    private String requirementId;
    private String parentId;
    private String teacherId;
    private Integer orderStatus;
    private BigDecimal orderAmount;
    private LocalDateTime orderStartTime;
    private LocalDateTime orderEndTime;
    private String orderRemark;
    private Integer orderAuditStatus;
    private Integer orderDeleteStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
