package com.teacher.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionProvinceNodeVO {
    private String name;
    private List<RegionCityNodeVO> cities;

    public RegionProvinceNodeVO(String name) {
        this.name = name;
        this.cities = Collections.emptyList();
    }
}
