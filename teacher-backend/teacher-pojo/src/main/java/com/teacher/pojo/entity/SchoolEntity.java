package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("school")
public class SchoolEntity {
    @TableId
    private String id;
    private String schoolName;
    private String schoolCode;
    private Integer schoolType;
    private String schoolProvince;
    private String schoolCity;
    private String schoolDistrict;
    private String schoolAddress;
    private BigDecimal schoolLongitude;
    private BigDecimal schoolLatitude;
    private Integer schoolStatus;
    private Integer schoolDeleteStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
