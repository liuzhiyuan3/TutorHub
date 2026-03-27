package com.teacher.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserRegionUpdateRequest {
    private String regionCode;
    private String regionName;
    private String regionProvince;
    private String regionCity;
    private String regionDistrict;
    private String regionSource;

    private String userLocationAddress;
    private BigDecimal userLocationLongitude;
    private BigDecimal userLocationLatitude;
}
