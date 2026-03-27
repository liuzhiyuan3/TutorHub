package com.teacher.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationReverseVO {
    private String address;
    private String province;
    private String city;
    private String district;
}

