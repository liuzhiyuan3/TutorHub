package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class UserEntity {
    @TableId
    private String id;
    private String userAccount;
    private String userPassword;
    private String userWechatOpenid;
    private String userName;
    private String userPortrait;
    private Integer userGender;
    private String userEmail;
    private String userPhone;
    private String userLocationAddress;
    private BigDecimal userLocationLongitude;
    private BigDecimal userLocationLatitude;
    private String userRegionCode;
    private String userRegionName;
    private String userRegionProvince;
    private String userRegionCity;
    private String userRegionDistrict;
    private String userRegionSource;
    private LocalDateTime userRegionSyncTime;
    private Integer userType;
    private Integer userStatus;
    private Integer profileCompleted;
    private String nicknameSource;
    private String avatarSource;
    private LocalDateTime lastProfileCompleteTime;
    private Integer userDeleteStatus;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
