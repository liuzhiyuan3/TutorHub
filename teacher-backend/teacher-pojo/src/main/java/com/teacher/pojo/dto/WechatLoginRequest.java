package com.teacher.pojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WechatLoginRequest {
    @NotBlank(message = "微信code不能为空")
    private String code;

    @NotNull(message = "用户类型不能为空")
    @Min(value = 0, message = "用户类型不合法")
    @Max(value = 1, message = "用户类型不合法")
    private Integer userType;

    @Size(max = 64, message = "昵称长度不能超过64")
    private String nickName;

    @Size(max = 255, message = "头像地址长度不能超过255")
    private String avatarUrl;
}
