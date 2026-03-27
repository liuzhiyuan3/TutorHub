package com.teacher.pojo.dto.admin;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DictionaryContentSaveRequest {
    private String id;

    @NotBlank(message = "字典ID不能为空")
    private String dictionaryId;

    @NotBlank(message = "字典项文本不能为空")
    @Size(max = 128, message = "字典项文本长度不能超过128")
    private String dictionaryContentText;

    @NotBlank(message = "字典项值不能为空")
    @Size(max = 128, message = "字典项值长度不能超过128")
    private String dictionaryContentValue;

    @NotNull(message = "字典项排序不能为空")
    @Min(value = 0, message = "字典项排序不能小于0")
    private Integer dictionaryContentSort;

    @NotNull(message = "字典项状态不能为空")
    @Min(value = 0, message = "字典项状态不合法")
    @Max(value = 1, message = "字典项状态不合法")
    private Integer dictionaryContentStatus;
}
