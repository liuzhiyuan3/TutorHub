package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("teacher_success_record")
public class TeacherSuccessRecordEntity {
    @TableId
    private String id;
    private String teacherId;
    private String orderId;
    private String successGrade;
    private LocalDateTime successOrderDate;
    private String successDescription;
    private Integer successDeleteStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
