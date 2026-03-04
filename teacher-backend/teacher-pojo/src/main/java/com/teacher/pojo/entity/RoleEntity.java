package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("role")
public class RoleEntity {
    @TableId
    private String id;
    private String roleName;
    private String roleCode;
    private String roleDescription;
    private Integer roleDeleteStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
