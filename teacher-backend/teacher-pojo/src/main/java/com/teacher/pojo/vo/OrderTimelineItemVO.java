package com.teacher.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderTimelineItemVO {
    private String nodeCode;
    private String nodeName;
    private Integer fromStatus;
    private Integer toStatus;
    private String operatorType;
    private String operatorId;
    private String operatorName;
    private LocalDateTime operateTime;
    private String remark;
}
