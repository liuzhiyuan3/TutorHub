package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("teacher_audit")
public class TeacherAuditEntity {
    @TableId
    private String id;
    private String teacherId;
    private Integer auditStatus;
    private String auditReason;
    private String auditorId;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
