package com.teacher.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PublicAdvertisingVO {
    private String id;
    private String title;
    private String image;
    private String link;
    private LocalDateTime expireTime;
}
