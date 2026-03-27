package com.teacher.pojo.dto.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SlideSaveRequest {
    private String id;

    @NotBlank(message = "轮播图图片不能为空")
    @Size(max = 255, message = "轮播图图片长度不能超过255")
    private String slidePicture;

    @Size(max = 255, message = "轮播图跳转链接长度不能超过255")
    private String slideLink;

    @Size(max = 255, message = "轮播图备注长度不能超过255")
    private String slideNote;

    @NotNull(message = "轮播图优先级不能为空")
    @Min(value = 0, message = "轮播图优先级不能小于0")
    private Integer slidePriority;

    @NotNull(message = "轮播图状态不能为空")
    @Min(value = 0, message = "轮播图状态不合法")
    @Max(value = 1, message = "轮播图状态不合法")
    private Integer slideStatus;

    @NotNull(message = "轮播图模块不能为空")
    @Min(value = 0, message = "轮播图模块不合法")
    private Integer slideModule;
}
