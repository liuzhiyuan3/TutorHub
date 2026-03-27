package com.teacher.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeacherSuccessRecordVO {
    private String successGrade;
    private LocalDateTime successOrderDate;
    private String successDescription;
}
