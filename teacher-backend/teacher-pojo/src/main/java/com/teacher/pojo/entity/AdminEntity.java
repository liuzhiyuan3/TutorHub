package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin")
public class AdminEntity {
    @TableId
    private String id;
    private String adminAccount;
    private String adminPassword;
    private String adminName;
    private String adminPortrait;
    private Integer adminGender;
    private String adminEmail;
    private String adminPhone;
    private String roleId;
    private Integer adminEnableStatus;
    private Integer adminDeleteStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
