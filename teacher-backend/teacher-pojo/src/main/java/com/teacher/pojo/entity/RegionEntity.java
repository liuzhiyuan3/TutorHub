package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("region")
public class RegionEntity {
    @TableId
    private String id;
    private String regionName;
    private String regionCode;
    private String regionCity;
    private String regionProvince;
    private Integer regionSort;
    private Integer regionStatus;
    private Integer regionDeleteStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
