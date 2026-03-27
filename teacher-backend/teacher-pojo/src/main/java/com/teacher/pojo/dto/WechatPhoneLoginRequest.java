package com.teacher.pojo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WechatPhoneLoginRequest {
    @NotBlank(message = "微信code不能为空")
    private String code;

    @NotBlank(message = "手机号code不能为空")
    private String phoneCode;

    @NotNull(message = "用户类型不能为空")
    @Min(value = 0, message = "用户类型不合法")
    @Max(value = 1, message = "用户类型不合法")
    private Integer userType;
}
