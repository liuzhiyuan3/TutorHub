package com.teacher.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginVO {
    private String token;
    private String userId;
    private String account;
    private String name;
    private Integer userType;
    private Boolean admin;
}
