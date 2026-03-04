package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.common.exception.BusinessException;
import com.teacher.common.model.PageResult;
import com.teacher.common.security.LoginUser;
import com.teacher.common.util.IdUtil;
import com.teacher.pojo.dto.OrderStatusUpdateRequest;
import com.teacher.pojo.entity.OrderEntity;
import com.teacher.pojo.entity.RequirementEntity;
import com.teacher.pojo.entity.TeacherInfoEntity;
import com.teacher.server.mapper.OrderMapper;
import com.teacher.server.mapper.RequirementMapper;
import com.teacher.server.mapper.TeacherInfoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderService {
    private final OrderMapper orderMapper;
    private final RequirementMapper requirementMapper;
    private final TeacherInfoMapper teacherInfoMapper;
    private final AuthService authService;

    public OrderService(OrderMapper orderMapper, RequirementMapper requirementMapper, TeacherInfoMapper teacherInfoMapper, AuthService authService) {
        this.orderMapper = orderMapper;
        this.requirementMapper = requirementMapper;
        this.teacherInfoMapper = teacherInfoMapper;
        this.authService = authService;
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderEntity receiveRequirement(String requirementId) {
        LoginUser loginUser = authService.currentLoginUser();
        if (loginUser.getUserType() == null || loginUser.getUserType() != 1) {
            throw new BusinessException("仅教员可接单");
        }
        TeacherInfoEntity teacher = teacherInfoMapper.selectOne(new LambdaQueryWrapper<TeacherInfoEntity>()
                .eq(TeacherInfoEntity::getUserId, loginUser.getId())
                .eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)
                .last("limit 1"));
        if (teacher == null || teacher.getTeacherAuditStatus() == null || teacher.getTeacherAuditStatus() != 1) {
            throw new BusinessException("请先完成教员资料并通过审核");
        }
        RequirementEntity requirement = requirementMapper.selectById(requirementId);
        if (requirement == null || requirement.getRequirementDeleteStatus() == 1) {
            throw new BusinessException("需求不存在");
        }
        if (requirement.getRequirementStatus() != 0) {
            throw new BusinessException("该需求已被接单");
        }
        requirement.setRequirementStatus(1);
        requirement.setUpdateTime(LocalDateTime.now());
        requirementMapper.updateById(requirement);

        OrderEntity order = new OrderEntity();
        order.setId(IdUtil.uuid32());
        order.setOrderNumber("OD" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));
        order.setRequirementId(requirement.getId());
        order.setParentId(requirement.getParentId());
        order.setTeacherId(teacher.getId());
        order.setOrderStatus(0);
        order.setOrderAmount(requirement.getRequirementSalary());
        order.setOrderAuditStatus(1);
        order.setOrderDeleteStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);
        return order;
    }

    public PageResult<OrderEntity> myOrders(long pageNo, long pageSize) {
        LoginUser loginUser = authService.currentLoginUser();
        Page<OrderEntity> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<OrderEntity> wrapper = new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderDeleteStatus, 0)
                .orderByDesc(OrderEntity::getCreateTime);
        if (loginUser.getUserType() != null && loginUser.getUserType() == 0) {
            wrapper.eq(OrderEntity::getParentId, loginUser.getId());
        } else {
            TeacherInfoEntity teacher = teacherInfoMapper.selectOne(new LambdaQueryWrapper<TeacherInfoEntity>()
                    .eq(TeacherInfoEntity::getUserId, loginUser.getId())
                    .eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)
                    .last("limit 1"));
            if (teacher == null) {
                throw new BusinessException("教员信息不存在");
            }
            wrapper.eq(OrderEntity::getTeacherId, teacher.getId());
        }
        Page<OrderEntity> result = orderMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), pageNo, pageSize, result.getRecords());
    }

    public PageResult<OrderEntity> adminPage(long pageNo, long pageSize, Integer status) {
        Page<OrderEntity> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<OrderEntity> wrapper = new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderDeleteStatus, 0)
                .orderByDesc(OrderEntity::getCreateTime);
        if (status != null) {
            wrapper.eq(OrderEntity::getOrderStatus, status);
        }
        Page<OrderEntity> result = orderMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), pageNo, pageSize, result.getRecords());
    }

    public OrderEntity updateStatus(String id, OrderStatusUpdateRequest request) {
        OrderEntity order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setOrderStatus(request.getOrderStatus());
        order.setOrderRemark(request.getOrderRemark());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return order;
    }
}
