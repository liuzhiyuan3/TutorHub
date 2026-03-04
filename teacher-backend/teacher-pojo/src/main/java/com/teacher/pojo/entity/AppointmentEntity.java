package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("appointment")
public class AppointmentEntity {
    @TableId
    private String id;
    private String parentId;
    private String teacherId;
    private String appointmentSubject;
    private String appointmentGrade;
    private String appointmentAddress;
    private LocalDateTime appointmentTime;
    private String appointmentRemark;
    private Integer appointmentStatus;
    private Integer appointmentDeleteStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
