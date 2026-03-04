package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("teacher_subject")
public class TeacherSubjectEntity {
    @TableId
    private String id;
    private String teacherId;
    private String subjectId;
    private LocalDateTime createTime;
}
