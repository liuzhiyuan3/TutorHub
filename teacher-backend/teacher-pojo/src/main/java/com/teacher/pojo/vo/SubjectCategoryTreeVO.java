package com.teacher.pojo.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SubjectCategoryTreeVO {
    private String id;
    private String categoryName;
    private String categoryCode;
    private Integer categorySort;
    private List<PublicOptionVO> subjects = new ArrayList<>();
}
