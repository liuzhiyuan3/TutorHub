package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("role_menu")
public class RoleMenuEntity {
    @TableId
    private String id;
    private String roleId;
    private String menuId;
    private LocalDateTime createTime;
}
