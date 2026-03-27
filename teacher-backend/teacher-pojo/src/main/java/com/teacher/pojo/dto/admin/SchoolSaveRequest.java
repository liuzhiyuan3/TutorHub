package com.teacher.pojo.dto.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SchoolSaveRequest {
    private String id;

    @NotBlank(message = "学校名称不能为空")
    @Size(max = 128, message = "学校名称长度不能超过128")
    private String schoolName;

    @NotBlank(message = "学校编码不能为空")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "学校编码仅支持大写字母、数字、下划线")
    private String schoolCode;

    @NotNull(message = "学校类型不能为空")
    @Min(value = 0, message = "学校类型不合法")
    private Integer schoolType;

    @NotBlank(message = "学校省份不能为空")
    @Size(max = 32, message = "学校省份长度不能超过32")
    private String schoolProvince;

    @NotBlank(message = "学校城市不能为空")
    @Size(max = 32, message = "学校城市长度不能超过32")
    private String schoolCity;

    @NotBlank(message = "学校区县不能为空")
    @Size(max = 32, message = "学校区县长度不能超过32")
    private String schoolDistrict;

    @Size(max = 255, message = "学校地址长度不能超过255")
    @NotBlank(message = "学校地址不能为空")
    private String schoolAddress;

    @Digits(integer = 10, fraction = 6, message = "经度格式不合法")
    private BigDecimal schoolLongitude;

    @Digits(integer = 10, fraction = 6, message = "纬度格式不合法")
    private BigDecimal schoolLatitude;

    @NotNull(message = "学校状态不能为空")
    @Min(value = 0, message = "学校状态不合法")
    @Max(value = 1, message = "学校状态不合法")
    private Integer schoolStatus;
}
