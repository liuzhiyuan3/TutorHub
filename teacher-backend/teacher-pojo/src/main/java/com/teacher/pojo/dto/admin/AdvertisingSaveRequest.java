package com.teacher.pojo.dto.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdvertisingSaveRequest {
    private String id;

    @Size(max = 64, message = "广告来源长度不能超过64")
    private String advertisingSource;

    @NotBlank(message = "广告标题不能为空")
    @Size(max = 128, message = "广告标题长度不能超过128")
    private String advertisingTitle;

    @Size(max = 255, message = "广告链接长度不能超过255")
    private String advertisingLink;

    @NotBlank(message = "广告图片不能为空")
    @Size(max = 255, message = "广告图片长度不能超过255")
    private String advertisingPicture;

    @NotNull(message = "广告状态不能为空")
    @Min(value = 0, message = "广告状态不合法")
    @Max(value = 1, message = "广告状态不合法")
    private Integer advertisingStatus;

    private LocalDateTime advertisingExpireTime;
}
