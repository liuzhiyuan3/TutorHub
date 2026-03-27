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
import java.util.*;

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
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(6);
        }
        Map<String, Long> totals = new HashMap<>();
        totals.put("userTotal", userMapper.selectCount(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUserDeleteStatus, 0)));
        totals.put("orderTotal", orderMapper.selectCount(new LambdaQueryWrapper<OrderEntity>().eq(OrderEntity::getOrderDeleteStatus, 0)));
        totals.put("requirementTotal", requirementMapper.selectCount(new LambdaQueryWrapper<RequirementEntity>().eq(RequirementEntity::getRequirementDeleteStatus, 0)));

        List<Map<String, Object>> orderTrend = new ArrayList<>();
        List<Map<String, Object>> requirementTrend = new ArrayList<>();
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
            orderTrend.add(point(cursor, orderCnt));
            requirementTrend.add(point(cursor, requirementCnt));
            cursor = cursor.plusDays(1);
        }

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

    private Map<String, Object> point(LocalDate date, long value) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", date.toString());
        map.put("value", value);
        return map;
    }
}
