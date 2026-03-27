package com.teacher.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DispatchPublicListItemVO {
    private String orderId;
    private String orderNumber;
    private String regionName;
    private String subjectName;
    private String requirementGrade;
    private String summary;
    private LocalDateTime dispatchTime;
}
