package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teacher.pojo.entity.OrderEntity;
import com.teacher.pojo.enums.OrderStatusEnum;
import com.teacher.server.mapper.OrderMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TeacherOrderStatsService {
    private final OrderMapper orderMapper;

    public TeacherOrderStatsService(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    public Map<String, TeacherOrderStats> buildTeacherOrderStatsMap(List<String> teacherIds) {
        if (teacherIds == null || teacherIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<OrderEntity> orders = orderMapper.selectList(new LambdaQueryWrapper<OrderEntity>()
                .in(OrderEntity::getTeacherId, teacherIds)
                .eq(OrderEntity::getOrderDeleteStatus, 0)
                .select(OrderEntity::getTeacherId, OrderEntity::getOrderStatus));
        if (orders.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, TeacherOrderStats> result = new HashMap<>();
        for (OrderEntity order : orders) {
            if (order.getTeacherId() == null) {
                continue;
            }
            TeacherOrderStats stats = result.computeIfAbsent(order.getTeacherId(), key -> new TeacherOrderStats());
            Integer status = order.getOrderStatus();
            if (status != null && status == OrderStatusEnum.FINISHED.getCode()) {
                stats.setHistoryDealCount(stats.getHistoryDealCount() + 1);
            }
            if (status != null
                    && (status == OrderStatusEnum.WAITING_CONFIRM.getCode()
                    || status == OrderStatusEnum.IN_PROGRESS.getCode()
                    || status == OrderStatusEnum.FINISHED.getCode())) {
                stats.setHireCount(stats.getHireCount() + 1);
            }
        }
        return result;
    }

    public TeacherOrderStats buildSingleTeacherOrderStats(String teacherId) {
        if (teacherId == null || teacherId.isBlank()) {
            return new TeacherOrderStats();
        }
        return buildTeacherOrderStatsMap(Collections.singletonList(teacherId))
                .getOrDefault(teacherId, new TeacherOrderStats());
    }

    public static class TeacherOrderStats {
        private int historyDealCount;
        private int hireCount;

        public int getHistoryDealCount() {
            return historyDealCount;
        }

        public void setHistoryDealCount(int historyDealCount) {
            this.historyDealCount = historyDealCount;
        }

        public int getHireCount() {
            return hireCount;
        }

        public void setHireCount(int hireCount) {
            this.hireCount = hireCount;
        }
    }
}
