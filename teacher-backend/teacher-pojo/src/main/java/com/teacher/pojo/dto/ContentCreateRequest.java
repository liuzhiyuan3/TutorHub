package com.teacher.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContentCreateRequest {
    @NotBlank(message = "名称不能为空")
    private String name;
    private String code;
}
