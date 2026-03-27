package com.teacher.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginVO {
    private String token;
    private String userId;
    private String account;
    private String name;
    private Integer userType;
    private Boolean admin;
    private String openid;
    private Boolean profileCompleted;

    public LoginVO(String token, String userId, String account, String name, Integer userType, Boolean admin) {
        this(token, userId, account, name, userType, admin, null, null);
    }

    public LoginVO(String token, String userId, String account, String name, Integer userType, Boolean admin, String openid) {
        this(token, userId, account, name, userType, admin, openid, null);
    }

    public LoginVO(String token, String userId, String account, String name, Integer userType, Boolean admin, String openid, Boolean profileCompleted) {
        this.token = token;
        this.userId = userId;
        this.account = account;
        this.name = name;
        this.userType = userType;
        this.admin = admin;
        this.openid = openid;
        this.profileCompleted = profileCompleted;
    }
}
