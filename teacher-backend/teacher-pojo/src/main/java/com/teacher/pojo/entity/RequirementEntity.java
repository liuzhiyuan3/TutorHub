package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("requirement")
public class RequirementEntity {
    @TableId
    private String id;
    private String parentId;
    private String requirementTitle;
    private String requirementDescription;
    private String subjectId;
    private String requirementGrade;
    private String regionId;
    private String requirementAddress;
    private Integer requirementTutoringMethod;
    private String requirementFrequency;
    private BigDecimal requirementSalary;
    private String requirementOther;
    private String requirementImages;
    private Integer requirementStatus;
    private Integer requirementAuditStatus;
    private Integer requirementDeleteStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
