package com.teacher.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeacherProfileRequest {
    @NotBlank(message = "教员身份不能为空")
    private String teacherIdentity;
    @NotNull(message = "辅导方式不能为空")
    private Integer teacherTutoringMethod;
    private Integer teacherTeachingYears;
    private String teacherSchool;
    private String teacherMajor;
    private String teacherEducation;
    private String teacherPhoto;
    private String teacherExperience;
    private String teacherSelfDescription;
}
