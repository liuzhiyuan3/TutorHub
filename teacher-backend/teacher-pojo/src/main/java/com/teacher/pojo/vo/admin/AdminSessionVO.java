package com.teacher.pojo.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminSessionVO {
    private String id;
    private String account;
    private String name;
    private Boolean admin;
}

