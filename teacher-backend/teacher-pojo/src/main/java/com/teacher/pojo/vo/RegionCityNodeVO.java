package com.teacher.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionCityNodeVO {
    private String name;
    private List<RegionDistrictNodeVO> districts;

    public RegionCityNodeVO(String name) {
        this.name = name;
        this.districts = Collections.emptyList();
    }
}
