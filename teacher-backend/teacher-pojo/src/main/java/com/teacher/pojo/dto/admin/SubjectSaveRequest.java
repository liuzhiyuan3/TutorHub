package com.teacher.pojo.dto.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SubjectSaveRequest {
    private String id;

    @NotBlank(message = "学科名称不能为空")
    @Size(max = 64, message = "学科名称长度不能超过64")
    private String subjectName;

    @NotBlank(message = "学科编码不能为空")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "学科编码仅支持大写字母、数字、下划线")
    private String subjectCode;

    @Size(max = 64, message = "分类ID长度不能超过64")
    private String subjectCategoryId;

    @Size(max = 64, message = "学科分类长度不能超过64")
    private String subjectCategory;

    @Size(max = 255, message = "学科描述长度不能超过255")
    private String subjectDescription;

    @NotNull(message = "学科排序不能为空")
    @Min(value = 0, message = "学科排序不能小于0")
    private Integer subjectSort;

    @NotNull(message = "学科状态不能为空")
    @Min(value = 0, message = "学科状态不合法")
    @Max(value = 1, message = "学科状态不合法")
    private Integer subjectStatus;
}
