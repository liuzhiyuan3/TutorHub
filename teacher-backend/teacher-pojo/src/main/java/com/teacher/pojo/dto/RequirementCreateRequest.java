package com.teacher.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RequirementCreateRequest {
    @NotBlank(message = "需求标题不能为空")
    private String requirementTitle;
    @NotBlank(message = "需求描述不能为空")
    private String requirementDescription;
    @NotBlank(message = "学科ID不能为空")
    private String subjectId;
    @NotBlank(message = "年级不能为空")
    private String requirementGrade;
    @NotBlank(message = "区域ID不能为空")
    private String regionId;
    @NotBlank(message = "授课地址不能为空")
    private String requirementAddress;
    @NotNull(message = "授课方式不能为空")
    private Integer requirementTutoringMethod;
    @NotNull(message = "薪资不能为空")
    private BigDecimal requirementSalary;
    private String requirementFrequency;
    private String requirementOther;
}
