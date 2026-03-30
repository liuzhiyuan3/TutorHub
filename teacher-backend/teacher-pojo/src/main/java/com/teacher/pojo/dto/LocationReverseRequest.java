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

    /**
     * 可选：客户端透传的高德地图 key。为空时由后端配置 app.amap.key 兜底。
     */
    private String amapKey;

    /**
     * 兼容旧字段：历史前端可能仍透传 qqMapKey。
     */
    private String qqMapKey;
}
