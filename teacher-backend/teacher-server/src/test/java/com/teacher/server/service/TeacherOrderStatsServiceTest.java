package com.teacher.server.service;

import com.teacher.pojo.entity.OrderEntity;
import com.teacher.pojo.enums.OrderStatusEnum;
import com.teacher.server.mapper.OrderMapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherOrderStatsServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private TeacherOrderStatsService teacherOrderStatsService;

    TeacherOrderStatsServiceTest() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, OrderEntity.class);
    }

    @Test
    void buildTeacherOrderStatsMap_shouldCountByStatusRule() {
        OrderEntity waitingConfirm = order("t1", OrderStatusEnum.WAITING_CONFIRM.getCode());
        OrderEntity inProgress = order("t1", OrderStatusEnum.IN_PROGRESS.getCode());
        OrderEntity finished = order("t1", OrderStatusEnum.FINISHED.getCode());
        OrderEntity canceled = order("t1", OrderStatusEnum.CANCELED.getCode());
        OrderEntity otherTeacherFinished = order("t2", OrderStatusEnum.FINISHED.getCode());

        when(orderMapper.selectList(any())).thenReturn(Arrays.asList(
                waitingConfirm, inProgress, finished, canceled, otherTeacherFinished
        ));

        Map<String, TeacherOrderStatsService.TeacherOrderStats> result =
                teacherOrderStatsService.buildTeacherOrderStatsMap(Arrays.asList("t1", "t2"));

        assertEquals(1, result.get("t1").getHistoryDealCount());
        assertEquals(3, result.get("t1").getHireCount());
        assertEquals(1, result.get("t2").getHistoryDealCount());
        assertEquals(1, result.get("t2").getHireCount());
    }

    private OrderEntity order(String teacherId, Integer status) {
        OrderEntity order = new OrderEntity();
        order.setTeacherId(teacherId);
        order.setOrderStatus(status);
        order.setOrderDeleteStatus(0);
        return order;
    }
}
