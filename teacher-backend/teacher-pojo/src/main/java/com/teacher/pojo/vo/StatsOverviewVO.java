package com.teacher.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsOverviewVO {
    private Map<String, Long> totals;
    private List<Map<String, Object>> orderTrend;
    private List<Map<String, Object>> requirementTrend;
    private List<Map<String, Object>> regionDistribution;
}
