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
    private List<RegionProvinceNodeVO> regionTree;

    public FilterMetaVO(List<PublicOptionVO> subjects, List<PublicOptionVO> regions, List<PublicOptionVO> schools) {
        this(subjects, regions, schools, Collections.emptyList());
    }

    public FilterMetaVO(List<PublicOptionVO> subjects, List<PublicOptionVO> regions, List<PublicOptionVO> schools,
                        List<RegionProvinceNodeVO> regionTree) {
        this.subjects = subjects == null ? Collections.emptyList() : subjects;
        this.regions = regions == null ? Collections.emptyList() : regions;
        this.schools = schools == null ? Collections.emptyList() : schools;
        this.regionTree = regionTree == null ? Collections.emptyList() : regionTree;
    }
}
