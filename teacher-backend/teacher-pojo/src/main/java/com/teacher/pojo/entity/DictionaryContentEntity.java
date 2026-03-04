package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dictionary_content")
public class DictionaryContentEntity {
    @TableId
    private String id;
    private String dictionaryContentText;
    private String dictionaryContentValue;
    private String dictionaryId;
    private Integer dictionaryContentSort;
    private Integer dictionaryContentStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
