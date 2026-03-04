package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dispatch_record")
public class DispatchRecordEntity {
    @TableId
    private String id;
    private String orderId;
    private String requirementId;
    private String parentId;
    private String teacherId;
    private LocalDateTime dispatchTime;
    private Integer dispatchStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
