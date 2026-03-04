package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class UserEntity {
    @TableId
    private String id;
    private String userAccount;
    private String userPassword;
    private String userName;
    private String userPortrait;
    private Integer userGender;
    private String userEmail;
    private String userPhone;
    private Integer userType;
    private Integer userStatus;
    private Integer userDeleteStatus;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
