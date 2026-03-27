package com.teacher.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserLocationUpdateRequest {
    private String userLocationAddress;
    @NotNull(message = "经度不能为空")
    private BigDecimal userLocationLongitude;
    @NotNull(message = "纬度不能为空")
    private BigDecimal userLocationLatitude;
}
