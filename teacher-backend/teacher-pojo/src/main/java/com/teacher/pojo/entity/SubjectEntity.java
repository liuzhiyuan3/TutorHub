package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("subject")
public class SubjectEntity {
    @TableId
    private String id;
    private String subjectName;
    private String subjectCode;
    private String subjectCategory;
    private String subjectDescription;
    private Integer subjectSort;
    private Integer subjectStatus;
    private Integer subjectDeleteStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
