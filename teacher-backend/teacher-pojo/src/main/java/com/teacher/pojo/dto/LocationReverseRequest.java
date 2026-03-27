package com.teacher.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LocationReverseRequest {
    @NotNull(message = "latitude不能为空")
    private BigDecimal latitude;

    @NotNull(message = "longitude不能为空")
    private BigDecimal longitude;
}

