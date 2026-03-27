package com.teacher.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RequirementCreateRequest {
    @NotBlank(message = "Requirement title is required")
    private String requirementTitle;

    @NotBlank(message = "Requirement description is required")
    private String requirementDescription;

    @NotBlank(message = "Subject is required")
    private String subjectId;

    @NotBlank(message = "Grade is required")
    private String requirementGrade;

    @NotBlank(message = "Region is required")
    private String regionId;

    @NotBlank(message = "Address is required")
    private String requirementAddress;

    private BigDecimal requirementLongitude;
    private BigDecimal requirementLatitude;

    @NotNull(message = "Tutoring method is required")
    private Integer requirementTutoringMethod;

    @NotNull(message = "Salary is required")
    private BigDecimal requirementSalary;

    @Size(max = 16, message = "Student gender max length is 16")
    private String studentGender;

    @Size(max = 100, message = "Salary text max length is 100")
    private String salaryText;

    @Size(max = 255, message = "Cross street max length is 255")
    private String crossStreet;

    @Size(max = 1000, message = "Student detail max length is 1000")
    private String studentDetail;

    @Size(max = 100, message = "Teacher qualification max length is 100")
    private String teacherQualification;

    @Size(max = 16, message = "Teacher gender preference max length is 16")
    private String teacherGenderPreference;

    @Size(max = 1000, message = "Teacher requirement max length is 1000")
    private String teacherRequirementText;

    private String requirementFrequency;
    private String requirementOther;
    private String requirementImages;
}
