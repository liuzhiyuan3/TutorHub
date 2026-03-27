package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("teacher_info")
public class TeacherInfoEntity {
    @TableId
    private String id;
    private String userId;
    private String teacherIdentity;
    private String teacherPhoto;
    private Integer teacherAge;
    private Integer teacherTeachingYears;
    private String teacherHometown;
    private String teacherHomeAddress;
    private String teacherWorkAddress;
    private BigDecimal teacherWorkLongitude;
    private BigDecimal teacherWorkLatitude;
    private String teacherSchool;
    private String teacherMajor;
    private String teacherEducation;
    private String teacherSelfDescription;
    private Integer teacherTutoringMethod;
    private String teacherExperience;
    private Integer teacherSuccessCount;
    private Integer teacherViewCount;
    private Integer teacherAuditStatus;
    private String teacherCertNo;
    private String teacherCertImages;
    private Integer teacherProfileCompleted;
    private Integer teacherEnableStatus;
    private Integer teacherDeleteStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
