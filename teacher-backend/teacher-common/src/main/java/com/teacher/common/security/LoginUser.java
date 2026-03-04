package com.teacher.common.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {
    private String id;
    private String account;
    private Integer userType;
    private boolean admin;
}
