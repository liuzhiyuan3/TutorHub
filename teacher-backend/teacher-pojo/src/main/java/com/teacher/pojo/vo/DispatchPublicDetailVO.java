package com.teacher.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DispatchPublicDetailVO {
    private String orderId;
    private String orderNumber;
    private String subjectName;
    private String requirementGrade;
    private String studentGender;
    private String salaryText;
    private String regionName;
    private String requirementAddress;
    private String crossStreet;
    private String requirementFrequency;
    private String studentDetail;
    private String teacherQualification;
    private String requirementTutoringMethodText;
    private String teacherGenderPreference;
    private String teacherRequirementText;
    private String summary;
    private LocalDateTime dispatchTime;
    private Boolean fullVisible;
}
