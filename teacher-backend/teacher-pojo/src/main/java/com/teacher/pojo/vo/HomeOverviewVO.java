package com.teacher.pojo.vo;

import com.teacher.pojo.entity.DispatchRecordEntity;
import com.teacher.pojo.entity.RegionEntity;
import com.teacher.pojo.entity.RequirementEntity;
import com.teacher.pojo.entity.SchoolEntity;
import com.teacher.pojo.entity.SubjectEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class HomeOverviewVO {
    private List<SubjectEntity> hotSubjects;
    private List<RegionEntity> hotRegions;
    private List<SchoolEntity> hotSchools;
    private List<RequirementEntity> latestRequirements;
    private List<DispatchRecordEntity> latestDispatches;
    private List<DispatchPublicListItemVO> latestDispatchCards;

    public HomeOverviewVO(List<SubjectEntity> hotSubjects, List<RegionEntity> hotRegions,
                          List<SchoolEntity> hotSchools, List<RequirementEntity> latestRequirements,
                          List<DispatchRecordEntity> latestDispatches,
                          List<DispatchPublicListItemVO> latestDispatchCards) {
        this.hotSubjects = hotSubjects == null ? Collections.emptyList() : hotSubjects;
        this.hotRegions = hotRegions == null ? Collections.emptyList() : hotRegions;
        this.hotSchools = hotSchools == null ? Collections.emptyList() : hotSchools;
        this.latestRequirements = latestRequirements == null ? Collections.emptyList() : latestRequirements;
        this.latestDispatches = latestDispatches == null ? Collections.emptyList() : latestDispatches;
        this.latestDispatchCards = latestDispatchCards == null ? Collections.emptyList() : latestDispatchCards;
    }
}
