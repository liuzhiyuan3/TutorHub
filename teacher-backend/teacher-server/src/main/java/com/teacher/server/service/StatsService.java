package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teacher.pojo.entity.OrderEntity;
import com.teacher.pojo.entity.RegionEntity;
import com.teacher.pojo.entity.RequirementEntity;
import com.teacher.pojo.entity.UserEntity;
import com.teacher.pojo.vo.StatsOverviewVO;
import com.teacher.server.mapper.OrderMapper;
import com.teacher.server.mapper.RegionMapper;
import com.teacher.server.mapper.RequirementMapper;
import com.teacher.server.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class StatsService {
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final RequirementMapper requirementMapper;
    private final RegionMapper regionMapper;

    public StatsService(UserMapper userMapper, OrderMapper orderMapper, RequirementMapper requirementMapper, RegionMapper regionMapper) {
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.requirementMapper = requirementMapper;
        this.regionMapper = regionMapper;
    }

    public StatsOverviewVO overview(LocalDate startDate, LocalDate endDate) {
        return overview(startDate, endDate, "day");
    }

    public StatsOverviewVO overview(LocalDate startDate, LocalDate endDate, String granularity) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(6);
        }
        String normalizedGranularity = normalizeGranularity(granularity);
        Map<String, Long> totals = new HashMap<>();
        totals.put("userTotal", userMapper.selectCount(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUserDeleteStatus, 0)));
        totals.put("orderTotal", orderMapper.selectCount(new LambdaQueryWrapper<OrderEntity>().eq(OrderEntity::getOrderDeleteStatus, 0)));
        totals.put("requirementTotal", requirementMapper.selectCount(new LambdaQueryWrapper<RequirementEntity>().eq(RequirementEntity::getRequirementDeleteStatus, 0)));

        Map<String, Long> orderBucketCounter = new LinkedHashMap<>();
        Map<String, Long> requirementBucketCounter = new LinkedHashMap<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            LocalDateTime from = cursor.atStartOfDay();
            LocalDateTime to = cursor.atTime(LocalTime.MAX);
            long orderCnt = orderMapper.selectCount(new LambdaQueryWrapper<OrderEntity>()
                    .between(OrderEntity::getCreateTime, from, to)
                    .eq(OrderEntity::getOrderDeleteStatus, 0));
            long requirementCnt = requirementMapper.selectCount(new LambdaQueryWrapper<RequirementEntity>()
                    .between(RequirementEntity::getCreateTime, from, to)
                    .eq(RequirementEntity::getRequirementDeleteStatus, 0));
            String bucket = toBucket(cursor, normalizedGranularity);
            orderBucketCounter.put(bucket, orderBucketCounter.getOrDefault(bucket, 0L) + orderCnt);
            requirementBucketCounter.put(bucket, requirementBucketCounter.getOrDefault(bucket, 0L) + requirementCnt);
            cursor = cursor.plusDays(1);
        }
        List<Map<String, Object>> orderTrend = toTrendPoints(orderBucketCounter);
        List<Map<String, Object>> requirementTrend = toTrendPoints(requirementBucketCounter);

        List<RegionEntity> regions = regionMapper.selectList(new LambdaQueryWrapper<RegionEntity>()
                .eq(RegionEntity::getRegionDeleteStatus, 0)
                .orderByAsc(RegionEntity::getRegionSort)
                .last("limit 10"));
        List<Map<String, Object>> regionDistribution = new ArrayList<>();
        for (RegionEntity region : regions) {
            long cnt = requirementMapper.selectCount(new LambdaQueryWrapper<RequirementEntity>()
                    .eq(RequirementEntity::getRegionId, region.getId())
                    .eq(RequirementEntity::getRequirementDeleteStatus, 0));
            Map<String, Object> row = new HashMap<>();
            row.put("regionId", region.getId());
            row.put("regionName", region.getRegionName());
            row.put("requirementCount", cnt);
            regionDistribution.add(row);
        }
        return new StatsOverviewVO(totals, orderTrend, requirementTrend, regionDistribution);
    }

    private List<Map<String, Object>> toTrendPoints(Map<String, Long> counter) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<String, Long> entry : counter.entrySet()) {
            rows.add(point(entry.getKey(), entry.getValue()));
        }
        return rows;
    }

    private String normalizeGranularity(String granularity) {
        String value = granularity == null ? "" : granularity.trim().toLowerCase();
        if ("week".equals(value) || "month".equals(value) || "day".equals(value)) {
            return value;
        }
        return "day";
    }

    private String toBucket(LocalDate date, String granularity) {
        if ("month".equals(granularity)) {
            return String.format("%d-%02d", date.getYear(), date.getMonthValue());
        }
        if ("week".equals(granularity)) {
            WeekFields weekFields = WeekFields.of(Locale.CHINA);
            int weekYear = date.get(weekFields.weekBasedYear());
            int week = date.get(weekFields.weekOfWeekBasedYear());
            return String.format("%d-W%02d", weekYear, week);
        }
        return date.toString();
    }

    private Map<String, Object> point(String date, long value) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", date);
        map.put("value", value);
        return map;
    }
}
