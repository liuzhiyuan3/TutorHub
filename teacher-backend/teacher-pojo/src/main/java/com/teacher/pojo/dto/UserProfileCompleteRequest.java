package com.teacher.pojo.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileCompleteRequest {
    @Size(max = 50, message = "昵称长度不能超过50")
    private String nickName;
    @Size(max = 255, message = "头像地址长度不能超过255")
    private String avatarUrl;
    @Size(max = 16, message = "角色标识长度不能超过16")
    private String role;

    @Size(max = 50, message = "教员身份长度不能超过50")
    private String teacherIdentity;
    private Integer teacherTutoringMethod;
    private Integer teacherTeachingYears;
    @Size(max = 100, message = "学校长度不能超过100")
    private String teacherSchool;
    @Size(max = 100, message = "专业长度不能超过100")
    private String teacherMajor;
    @Size(max = 50, message = "学历长度不能超过50")
    private String teacherEducation;
    @Size(max = 64, message = "教员资质编号长度不能超过64")
    private String teacherCertNo;
    private String teacherCertImages;
    private String teacherExperience;
    private String teacherSelfDescription;
}
