package com.teacher.pojo.dto.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegionSaveRequest {
    private String id;

    @NotBlank(message = "区域名称不能为空")
    @Size(max = 64, message = "区域名称长度不能超过64")
    private String regionName;

    @NotBlank(message = "区域编码不能为空")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "区域编码仅支持大写字母、数字、下划线")
    private String regionCode;

    @NotBlank(message = "区域城市不能为空")
    @Size(max = 32, message = "区域城市长度不能超过32")
    private String regionCity;

    @NotBlank(message = "区域省份不能为空")
    @Size(max = 32, message = "区域省份长度不能超过32")
    private String regionProvince;

    @NotNull(message = "区域排序不能为空")
    @Min(value = 0, message = "区域排序不能小于0")
    private Integer regionSort;

    @NotNull(message = "区域状态不能为空")
    @Min(value = 0, message = "区域状态不合法")
    @Max(value = 1, message = "区域状态不合法")
    private Integer regionStatus;
}
