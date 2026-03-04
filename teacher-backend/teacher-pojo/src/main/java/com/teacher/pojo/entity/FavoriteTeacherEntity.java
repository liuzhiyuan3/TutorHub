package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("favorite_teacher")
public class FavoriteTeacherEntity {
    @TableId
    private String id;
    private String parentId;
    private String teacherId;
    private LocalDateTime createTime;
}
