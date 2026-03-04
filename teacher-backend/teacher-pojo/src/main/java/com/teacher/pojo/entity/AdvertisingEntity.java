package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("advertising")
public class AdvertisingEntity {
    @TableId
    private String id;
    private String advertisingSource;
    private String advertisingTitle;
    private String advertisingLink;
    private String advertisingPicture;
    private Integer advertisingStatus;
    private LocalDateTime advertisingExpireTime;
    private Integer advertisingDeleteStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
