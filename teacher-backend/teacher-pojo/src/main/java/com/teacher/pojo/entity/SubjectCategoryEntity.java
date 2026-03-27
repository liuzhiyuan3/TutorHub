package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("subject_category")
public class SubjectCategoryEntity {
    @TableId
    private String id;
    private String categoryName;
    private String categoryCode;
    private Integer categorySort;
    private Integer categoryStatus;
    private Integer categoryDeleteStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
