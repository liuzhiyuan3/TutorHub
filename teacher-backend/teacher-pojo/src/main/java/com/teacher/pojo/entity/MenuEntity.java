package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("menu")
public class MenuEntity {
    @TableId
    private String id;
    private String menuName;
    private String menuParent;
    private Integer menuPriority;
    private String menuLink;
    private String menuIcon;
    private Integer menuType;
    private Integer menuDeleteStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
