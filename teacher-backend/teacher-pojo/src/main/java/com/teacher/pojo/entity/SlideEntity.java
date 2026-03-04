package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("slide")
public class SlideEntity {
    @TableId
    private String id;
    private String slidePicture;
    private String slideLink;
    private String slideNote;
    private Integer slidePriority;
    private Integer slideStatus;
    private Integer slideModule;
    private Integer slideDeleteStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
