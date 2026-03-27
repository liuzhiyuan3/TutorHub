package com.teacher.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class FilterMetaVO {
    private List<PublicOptionVO> subjects;
    private List<PublicOptionVO> regions;
    private List<PublicOptionVO> schools;

    public FilterMetaVO(List<PublicOptionVO> subjects, List<PublicOptionVO> regions, List<PublicOptionVO> schools) {
        this.subjects = subjects == null ? Collections.emptyList() : subjects;
        this.regions = regions == null ? Collections.emptyList() : regions;
        this.schools = schools == null ? Collections.emptyList() : schools;
    }
}
