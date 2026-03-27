package com.teacher.pojo.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleSaveRequest {
    private String id;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 64, message = "角色名称长度不能超过64")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "角色编码仅支持大写字母、数字、下划线")
    private String roleCode;

    @Size(max = 255, message = "角色描述长度不能超过255")
    private String roleDescription;
}
