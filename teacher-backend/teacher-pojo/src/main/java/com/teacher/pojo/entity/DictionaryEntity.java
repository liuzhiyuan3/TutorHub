package com.teacher.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dictionary")
public class DictionaryEntity {
    @TableId
    private String id;
    private String dictionaryName;
    private String dictionaryCode;
    private String dictionaryDescription;
    private Integer dictionaryDeleteStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
