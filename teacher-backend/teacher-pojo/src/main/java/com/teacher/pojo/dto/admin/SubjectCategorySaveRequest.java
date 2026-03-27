package com.teacher.pojo.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubjectCategorySaveRequest {
    private String id;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称长度不能超过64")
    private String categoryName;

    @NotBlank(message = "分类编码不能为空")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "分类编码仅支持大写字母、数字、下划线")
    private String categoryCode;

    @NotNull(message = "分类排序不能为空")
    @Min(value = 0, message = "分类排序不能小于0")
    private Integer categorySort;

    @NotNull(message = "分类状态不能为空")
    @Min(value = 0, message = "分类状态不合法")
    @Max(value = 1, message = "分类状态不合法")
    private Integer categoryStatus;
}
