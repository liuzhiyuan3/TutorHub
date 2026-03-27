package com.teacher.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class PublicRequirementListItemVO {
    private String id;
    private String requirementTitle;
    private String requirementGrade;
    private String requirementAddress;
    private Integer requirementTutoringMethod;
    private BigDecimal requirementSalary;
    private BigDecimal requirementBudgetMin;
    private BigDecimal requirementBudgetMax;
    private Integer requirementUrgency;
    private String requirementUrgencyText;
    private LocalDateTime requirementExpectedStartTime;
    private List<String> requirementExpectedTimeSlots = new ArrayList<>();
    private BigDecimal distanceKm;
    private String subjectId;
    private String subjectName;
    private String regionId;
    private String regionName;
    private Integer requirementStatus;
    private String requirementStatusText;
    private String teacherProfileVisibility;
    private LocalDateTime createTime;
}
