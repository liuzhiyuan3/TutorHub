package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("teacher_region")
public class TeacherRegionEntity {
    @TableId
    private String id;
    private String teacherId;
    private String regionId;
    private LocalDateTime createTime;
}
