package com.teacher.pojo.dto.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MenuSaveRequest {
    private String id;

    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 64, message = "菜单名称长度不能超过64")
    private String menuName;

    @Size(max = 64, message = "父菜单ID长度不能超过64")
    private String menuParent;

    @NotNull(message = "菜单排序不能为空")
    @Min(value = 0, message = "菜单排序不能小于0")
    private Integer menuPriority;

    @Size(max = 128, message = "菜单链接长度不能超过128")
    private String menuLink;

    @Size(max = 64, message = "菜单图标长度不能超过64")
    private String menuIcon;

    @NotNull(message = "菜单类型不能为空")
    @Min(value = 0, message = "菜单类型不合法")
    @Max(value = 2, message = "菜单类型不合法")
    private Integer menuType;
}
