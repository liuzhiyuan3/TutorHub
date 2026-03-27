package com.teacher.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RequirementPublicDetailVO {
    private String id;
    private String requirementTitle;
    private String requirementDescription;
    private String requirementGrade;
    private String requirementAddress;
    private Integer requirementTutoringMethod;
    private String requirementFrequency;
    private BigDecimal requirementSalary;
    private String studentGender;
    private String salaryText;
    private String crossStreet;
    private String studentDetail;
    private String teacherQualification;
    private String teacherGenderPreference;
    private String teacherRequirementText;
    private String requirementOther;
    private String requirementImages;
    private String subjectId;
    private String subjectName;
    private String regionId;
    private String regionName;
    private LocalDateTime createTime;
}
