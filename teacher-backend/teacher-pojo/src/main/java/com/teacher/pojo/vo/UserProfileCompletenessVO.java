package com.teacher.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserProfileCompletenessVO {
    private Boolean ready;
    private String role;
    private String userName;
    private String userPortrait;
    private List<String> missingFields;
}
