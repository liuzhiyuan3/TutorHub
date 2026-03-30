package com.teacher.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.common.exception.BusinessException;
import com.teacher.common.model.PageResult;
import com.teacher.common.security.LoginUser;
import com.teacher.common.util.IdUtil;
import com.teacher.pojo.dto.OrderStatusUpdateRequest;
import com.teacher.pojo.dto.AuditRequest;
import com.teacher.pojo.entity.DispatchRecordEntity;
import com.teacher.pojo.entity.OrderEntity;
import com.teacher.pojo.entity.RequirementEntity;
import com.teacher.pojo.entity.TeacherInfoEntity;
import com.teacher.pojo.entity.UserEntity;
import com.teacher.pojo.enums.AuditStatusEnum;
import com.teacher.pojo.enums.OrderStatusEnum;
import com.teacher.pojo.enums.RequirementStatusEnum;
import com.teacher.pojo.vo.OrderListItemVO;
import com.teacher.pojo.vo.OrderTimelineItemVO;
import com.teacher.pojo.vo.ParentOrderPoolItemVO;
import com.teacher.pojo.vo.UserProfileCompletenessVO;
import com.teacher.server.mapper.DispatchRecordMapper;
import com.teacher.server.mapper.OrderMapper;
import com.teacher.server.mapper.RequirementMapper;
import com.teacher.server.mapper.TeacherInfoMapper;
import com.teacher.server.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final Set<String> NON_BLOCKING_PROFILE_FIELDS = Set.of("nickName", "avatarUrl");
    private final OrderMapper orderMapper;
    private final RequirementMapper requirementMapper;
    private final TeacherInfoMapper teacherInfoMapper;
    private final UserMapper userMapper;
    private final DispatchRecordMapper dispatchRecordMapper;
    private final AuthService authService;
    private final UserService userService;

    public OrderService(OrderMapper orderMapper, RequirementMapper requirementMapper, TeacherInfoMapper teacherInfoMapper,
                        UserMapper userMapper, DispatchRecordMapper dispatchRecordMapper, AuthService authService,
                        UserService userService) {
        this.orderMapper = orderMapper;
        this.requirementMapper = requirementMapper;
        this.teacherInfoMapper = teacherInfoMapper;
        this.userMapper = userMapper;
        this.dispatchRecordMapper = dispatchRecordMapper;
        this.authService = authService;
        this.userService = userService;
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderEntity receiveRequirement(String requirementId) {
        LoginUser loginUser = authService.currentLoginUser();
        if (loginUser.getUserType() == null || loginUser.getUserType() != 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only teacher can accept orders");
        }

        UserProfileCompletenessVO completeness = userService.profileCompleteness();
        List<String> blockingMissingFields = resolveBlockingMissingFields(completeness);
        if (!blockingMissingFields.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Profile is incomplete: " + toChineseMissingFields(blockingMissingFields));
        }

        TeacherInfoEntity teacher = teacherInfoMapper.selectOne(new LambdaQueryWrapper<TeacherInfoEntity>()
                .eq(TeacherInfoEntity::getUserId, loginUser.getId())
                .eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)
                .last("limit 1"));
        if (teacher == null || teacher.getTeacherAuditStatus() == null || teacher.getTeacherAuditStatus() != 1) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Please complete profile and pass audit before accepting");
        }

        RequirementEntity requirement = requirementMapper.selectById(requirementId);
        if (requirement == null || requirement.getRequirementDeleteStatus() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Requirement not found");
        }
        if (requirement.getRequirementStatus() == null
                || requirement.getRequirementStatus() != RequirementStatusEnum.WAITING.getCode()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Requirement has already been accepted");
        }
        if (requirement.getRequirementAuditStatus() == null
                || requirement.getRequirementAuditStatus() != AuditStatusEnum.APPROVED.getCode()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Only approved requirements can be accepted");
        }

        int updated = requirementMapper.update(
                null,
                new LambdaUpdateWrapper<RequirementEntity>()
                        .set(RequirementEntity::getRequirementStatus, RequirementStatusEnum.RECEIVED.getCode())
                        .set(RequirementEntity::getUpdateTime, LocalDateTime.now())
                        .eq(RequirementEntity::getId, requirementId)
                        .eq(RequirementEntity::getRequirementStatus, RequirementStatusEnum.WAITING.getCode())
                        .eq(RequirementEntity::getRequirementDeleteStatus, 0)
        );
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Requirement was accepted by another teacher");
        }

        OrderEntity order = new OrderEntity();
        order.setId(IdUtil.uuid32());
        order.setOrderNumber("OD" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));
        order.setRequirementId(requirement.getId());
        order.setParentId(requirement.getParentId());
        order.setTeacherId(teacher.getId());
        order.setOrderStatus(OrderStatusEnum.WAITING_CONFIRM.getCode());
        order.setOrderAmount(requirement.getRequirementSalary());
        order.setOrderAuditStatus(AuditStatusEnum.PENDING.getCode());
        order.setOrderDeleteStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);

        DispatchRecordEntity dispatch = new DispatchRecordEntity();
        dispatch.setId(IdUtil.uuid32());
        dispatch.setOrderId(order.getId());
        dispatch.setRequirementId(requirement.getId());
        dispatch.setParentId(requirement.getParentId());
        dispatch.setTeacherId(teacher.getId());
        dispatch.setDispatchTime(LocalDateTime.now());
        dispatch.setDispatchStatus(1);
        dispatch.setCreateTime(LocalDateTime.now());
        dispatch.setUpdateTime(LocalDateTime.now());
        dispatchRecordMapper.insert(dispatch);
        return order;
    }

    public PageResult<OrderListItemVO> myOrders(long pageNo, long pageSize, Integer status, Integer payStatus, String keyword,
                                                BigDecimal amountMin, BigDecimal amountMax,
                                                LocalDateTime dateFrom, LocalDateTime dateTo, String sortBy) {
        LoginUser loginUser = authService.currentLoginUser();
        if (amountMin != null && amountMax != null && amountMin.compareTo(amountMax) > 0) {
            throw new BusinessException("Invalid amount range");
        }
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new BusinessException("Invalid date range");
        }
        Page<OrderEntity> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<OrderEntity> wrapper = new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getOrderDeleteStatus, 0);
        if (status != null) {
            wrapper.eq(OrderEntity::getOrderStatus, status);
        }
        if (amountMin != null) {
            wrapper.ge(OrderEntity::getOrderAmount, amountMin);
        }
        if (amountMax != null) {
            wrapper.le(OrderEntity::getOrderAmount, amountMax);
        }
        if (dateFrom != null) {
            wrapper.ge(OrderEntity::getCreateTime, dateFrom);
        }
        if (dateTo != null) {
            wrapper.le(OrderEntity::getCreateTime, dateTo);
        }
        if (loginUser.getUserType() != null && loginUser.getUserType() == 0) {
            wrapper.eq(OrderEntity::getParentId, loginUser.getId());
        } else if (loginUser.getUserType() != null && loginUser.getUserType() == 1) {
            TeacherInfoEntity teacher = teacherInfoMapper.selectOne(new LambdaQueryWrapper<TeacherInfoEntity>()
                    .eq(TeacherInfoEntity::getUserId, loginUser.getId())
                    .eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)
                    .last("limit 1"));
            if (teacher == null) {
                throw new BusinessException("Teacher profile not found");
            }
            wrapper.eq(OrderEntity::getTeacherId, teacher.getId());
        } else {
            throw new BusinessException("Current role cannot view orders");
        }
        if ("amountAsc".equalsIgnoreCase(sortBy)) {
            wrapper.orderByAsc(OrderEntity::getOrderAmount).orderByDesc(OrderEntity::getCreateTime);
        } else if ("amountDesc".equalsIgnoreCase(sortBy)) {
            wrapper.orderByDesc(OrderEntity::getOrderAmount).orderByDesc(OrderEntity::getCreateTime);
        } else {
            wrapper.orderByDesc(OrderEntity::getCreateTime);
        }
        Page<OrderEntity> result = orderMapper.selectPage(page, wrapper);
        List<OrderListItemVO> records = toOrderListItemVO(result.getRecords(), loginUser);
        if (payStatus != null || (keyword != null && !keyword.isBlank())) {
            records = records.stream()
                    .filter(item -> payStatus == null || payStatus.equals(item.getPayStatus()))
                    .filter(item -> matchKeyword(item, keyword))
                    .collect(Collectors.toList());
        }
        return new PageResult<>(result.getTotal(), pageNo, pageSize, records);
    }

    public PageResult<ParentOrderPoolItemVO> myOrderPoolPage(long pageNo, long pageSize) {
        LoginUser loginUser = authService.currentLoginUser();
        if (loginUser.getUserType() == null || loginUser.getUserType() != 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only parent can view order pool");
        }

        List<ParentOrderPoolItemVO> pool = new ArrayList<>();

        List<OrderEntity> orders = orderMapper.selectList(new LambdaQueryWrapper<OrderEntity>()
                .eq(OrderEntity::getParentId, loginUser.getId())
                .eq(OrderEntity::getOrderDeleteStatus, 0)
                .orderByDesc(OrderEntity::getCreateTime));
        List<OrderListItemVO> orderItems = toOrderListItemVO(orders, loginUser);
        for (OrderListItemVO item : orderItems) {
            ParentOrderPoolItemVO vo = new ParentOrderPoolItemVO();
            vo.setBizId(item.getId());
            vo.setSourceType("ORDER");
            vo.setParentOrderStage(resolveParentStageByOrder(item.getOrderStatus()));
            vo.setParentOrderStageText(resolveParentStageText(vo.getParentOrderStage()));
            vo.setRequirementStatus(resolveRequirementStatusByOrder(item.getOrderStatus()));
            vo.setOrderStatus(item.getOrderStatus());
            vo.setStatusText(item.getOrderStatusText());
            vo.setOrderNumber(item.getOrderNumber());
            vo.setRequirementId(item.getRequirementId());
            vo.setOrderAmount(item.getOrderAmount());
            vo.setServiceFee(item.getServiceFee());
            vo.setPayStatus(item.getPayStatus());
            vo.setPayStatusText(item.getPayStatusText());
            vo.setSubjectName(item.getSubjectName());
            vo.setCounterpartyName(item.getCounterpartyName());
            vo.setOrderStartTime(item.getOrderStartTime());
            vo.setOrderEndTime(item.getOrderEndTime());
            vo.setOrderRemark(item.getOrderRemark());
            vo.setOrderAuditStatus(item.getOrderAuditStatus());
            vo.setCanReview(item.getCanReview());
            vo.setCanDispute(item.getCanDispute());
            vo.setCreateTime(item.getCreateTime());
            vo.setUpdateTime(item.getUpdateTime());
            pool.add(vo);
        }

        List<String> matchedRequirementIds = orderItems.stream()
                .map(OrderListItemVO::getRequirementId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<RequirementEntity> waitingRequirements = requirementMapper.selectList(new LambdaQueryWrapper<RequirementEntity>()
                .eq(RequirementEntity::getParentId, loginUser.getId())
                .eq(RequirementEntity::getRequirementDeleteStatus, 0)
                .eq(RequirementEntity::getRequirementStatus, RequirementStatusEnum.WAITING.getCode())
                .orderByDesc(RequirementEntity::getCreateTime));
        if (!matchedRequirementIds.isEmpty()) {
            waitingRequirements = waitingRequirements.stream()
                    .filter(item -> !matchedRequirementIds.contains(item.getId()))
                    .collect(Collectors.toList());
        }
        for (RequirementEntity requirement : waitingRequirements) {
            ParentOrderPoolItemVO vo = new ParentOrderPoolItemVO();
            vo.setBizId(requirement.getId());
            vo.setSourceType("REQUIREMENT");
            vo.setParentOrderStage("PUBLISHED_WAITING");
            vo.setParentOrderStageText("等待接单");
            vo.setRequirementStatus(requirement.getRequirementStatus());
            vo.setOrderStatus(null);
            vo.setStatusText("待接单");
            vo.setOrderNumber("-");
            vo.setRequirementId(requirement.getId());
            vo.setOrderAmount(requirement.getRequirementSalary());
            vo.setServiceFee(BigDecimal.ZERO);
            vo.setPayStatus(0);
            vo.setPayStatusText("待支付");
            vo.setSubjectName(requirement.getRequirementTitle());
            vo.setCounterpartyName("-");
            vo.setOrderRemark(requirement.getRequirementDescription());
            vo.setOrderAuditStatus(requirement.getRequirementAuditStatus());
            vo.setCanReview(false);
            vo.setCanDispute(false);
            vo.setCreateTime(requirement.getCreateTime());
            vo.setUpdateTime(requirement.getUpdateTime());
            pool.add(vo);
        }

        pool.sort(Comparator.comparing(ParentOrderPoolItemVO::getCreateTime, Comparator.nullsLast(LocalDateTime::compareTo)).reversed());
        long total = pool.size();
        int start = (int) Math.max(0, (pageNo - 1) * pageSize);
        int end = (int) Math.min(total, start + pageSize);
        List<ParentOrderPoolItemVO> records = start >= end ? Collections.emptyList() : pool.subList(start, end);
        return new PageResult<>(total, pageNo, pageSize, records);
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
        LoginUser loginUser = authService.currentLoginUser();
        OrderEntity order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("Order not found");
        }
        if (!loginUser.isAdmin()) {
            if (loginUser.getUserType() != null && loginUser.getUserType() == 0) {
                if (!loginUser.getId().equals(order.getParentId())) {
                    throw new BusinessException("No permission to operate this order");
                }
            } else {
                TeacherInfoEntity teacher = teacherInfoMapper.selectOne(new LambdaQueryWrapper<TeacherInfoEntity>()
                        .eq(TeacherInfoEntity::getUserId, loginUser.getId())
                        .eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)
                        .last("limit 1"));
                if (teacher == null || !teacher.getId().equals(order.getTeacherId())) {
                    throw new BusinessException("No permission to operate this order");
                }
            }
        }
        if ((order.getOrderAuditStatus() == null || order.getOrderAuditStatus() != AuditStatusEnum.APPROVED.getCode())
                && !loginUser.isAdmin()) {
            throw new BusinessException("No permission to operate this order");
        }
        if (order.getOrderStatus() != null && order.getOrderStatus().equals(request.getOrderStatus())) {
            return order;
        }
        if (!loginUser.isAdmin()) {
            if (loginUser.getUserType() != null && loginUser.getUserType() == 0
                    && request.getOrderStatus() != null
                    && request.getOrderStatus() != OrderStatusEnum.CANCELED.getCode()) {
                throw new BusinessException("No permission to operate this order");
            }
            if (loginUser.getUserType() != null && loginUser.getUserType() == 1
                    && request.getOrderStatus() != null
                    && request.getOrderStatus() == OrderStatusEnum.CANCELED.getCode()) {
                throw new BusinessException("No permission to operate this order");
            }
        }
        if (!OrderStatusEnum.canTransit(order.getOrderStatus(), request.getOrderStatus())) {
            throw new BusinessException("Illegal order status transition");
        }
        order.setOrderStatus(request.getOrderStatus());
        order.setOrderRemark(request.getOrderRemark());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        syncRequirementStatus(order);
        return order;
    }

    public List<OrderTimelineItemVO> orderTimeline(String orderId) {
        LoginUser loginUser = authService.currentLoginUser();
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null || order.getOrderDeleteStatus() == 1) {
            throw new BusinessException("Order not found");
        }
        ensureOrderPermission(loginUser, order);
        Map<String, String> userNameMap = buildUserNameMap(order);
        List<OrderTimelineItemVO> timeline = new ArrayList<>();

        OrderTimelineItemVO created = new OrderTimelineItemVO();
        created.setNodeCode("created");
        created.setNodeName("Order Created");
        created.setFromStatus(null);
        created.setToStatus(OrderStatusEnum.WAITING_CONFIRM.getCode());
        created.setOperatorType("system");
        created.setOperatorId("0");
        created.setOperatorName("System");
        created.setOperateTime(order.getCreateTime());
        created.setRemark("Order created and waiting for confirmation");
        timeline.add(created);

        DispatchRecordEntity dispatch = dispatchRecordMapper.selectOne(new LambdaQueryWrapper<DispatchRecordEntity>()
                .eq(DispatchRecordEntity::getOrderId, orderId)
                .orderByAsc(DispatchRecordEntity::getDispatchTime)
                .last("limit 1"));
        if (dispatch != null) {
            OrderTimelineItemVO assigned = new OrderTimelineItemVO();
            assigned.setNodeCode("received");
            assigned.setNodeName("Teacher Accepted");
            assigned.setFromStatus(OrderStatusEnum.WAITING_CONFIRM.getCode());
            assigned.setToStatus(OrderStatusEnum.WAITING_CONFIRM.getCode());
            assigned.setOperatorType("teacher");
            assigned.setOperatorId(dispatch.getTeacherId());
            assigned.setOperatorName(userNameMap.getOrDefault(dispatch.getTeacherId(), "Teacher"));
            assigned.setOperateTime(dispatch.getDispatchTime());
            assigned.setRemark("Teacher accepted the order, waiting for confirmation");
            timeline.add(assigned);
        }

        if (order.getOrderStatus() != null && order.getOrderStatus() != OrderStatusEnum.WAITING_CONFIRM.getCode()) {
            OrderTimelineItemVO statusChanged = new OrderTimelineItemVO();
            statusChanged.setNodeCode("statusChanged");
            statusChanged.setNodeName("Status Changed");
            statusChanged.setFromStatus(OrderStatusEnum.WAITING_CONFIRM.getCode());
            statusChanged.setToStatus(order.getOrderStatus());
            statusChanged.setOperatorType(order.getOrderStatus() == OrderStatusEnum.CANCELED.getCode() ? "parent" : "teacher");
            statusChanged.setOperatorId(order.getOrderStatus() == OrderStatusEnum.CANCELED.getCode() ? order.getParentId() : order.getTeacherId());
            statusChanged.setOperatorName(userNameMap.getOrDefault(statusChanged.getOperatorId(), "User"));
            statusChanged.setOperateTime(order.getUpdateTime());
            statusChanged.setRemark(order.getOrderRemark() == null || order.getOrderRemark().isBlank()
                    ? ("Order status changed to " + statusText(order.getOrderStatus()))
                    : order.getOrderRemark());
            timeline.add(statusChanged);
        }

        timeline.sort(Comparator.comparing(OrderTimelineItemVO::getOperateTime, Comparator.nullsLast(LocalDateTime::compareTo)));
        return timeline;
    }

    public void adminAudit(String id, AuditRequest request) {
        authService.requireAdmin();
        if (!AuditStatusEnum.isValid(request.getAuditStatus())) {
            throw new BusinessException("Invalid audit status");
        }
        if (request.getAuditStatus() != null && request.getAuditStatus() == AuditStatusEnum.PENDING.getCode()) {
            throw new BusinessException("Cannot set audit status back to pending");
        }
        if (request.getAuditStatus() != null
                && request.getAuditStatus() == AuditStatusEnum.REJECTED.getCode()
                && (request.getReason() == null || request.getReason().isBlank())) {
            throw new BusinessException("Reject reason is required");
        }
        OrderEntity db = orderMapper.selectById(id);
        if (db == null || db.getOrderDeleteStatus() == 1) {
            throw new BusinessException("Order not found");
        }
        if (db.getOrderAuditStatus() != null && db.getOrderAuditStatus() != AuditStatusEnum.PENDING.getCode()) {
            throw new BusinessException("Order has been audited already");
        }
        if (request.getAuditStatus() != null && request.getAuditStatus() == AuditStatusEnum.APPROVED.getCode()) {
            if (db.getOrderStatus() == null || db.getOrderStatus() == OrderStatusEnum.CANCELED.getCode()) {
                throw new BusinessException("Canceled order cannot pass audit");
            }
        }
        db.setOrderAuditStatus(request.getAuditStatus());
        if (request.getAuditStatus() != null
                && request.getAuditStatus() == AuditStatusEnum.REJECTED.getCode()
                && db.getOrderStatus() != null
                && db.getOrderStatus() != OrderStatusEnum.FINISHED.getCode()) {
            db.setOrderStatus(OrderStatusEnum.CANCELED.getCode());
        }
        db.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(db);
        syncRequirementStatus(db);
    }

    public void syncRequirementStatusForExternal(OrderEntity order) {
        if (order == null) {
            return;
        }
        syncRequirementStatus(order);
    }

    private void syncRequirementStatus(OrderEntity order) {
        if (order.getOrderStatus() != null && order.getOrderStatus() == OrderStatusEnum.FINISHED.getCode()) {
            requirementMapper.update(
                    null,
                    new LambdaUpdateWrapper<RequirementEntity>()
                            .set(RequirementEntity::getRequirementStatus, RequirementStatusEnum.FINISHED.getCode())
                            .set(RequirementEntity::getUpdateTime, LocalDateTime.now())
                            .eq(RequirementEntity::getId, order.getRequirementId())
            );
            return;
        }
        if (order.getOrderStatus() != null && order.getOrderStatus() == OrderStatusEnum.CANCELED.getCode()) {
            requirementMapper.update(
                    null,
                    new LambdaUpdateWrapper<RequirementEntity>()
                            .set(RequirementEntity::getRequirementStatus, RequirementStatusEnum.CANCELED.getCode())
                            .set(RequirementEntity::getUpdateTime, LocalDateTime.now())
                            .eq(RequirementEntity::getId, order.getRequirementId())
            );
        }
    }

    private List<OrderListItemVO> toOrderListItemVO(List<OrderEntity> orders, LoginUser loginUser) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> requirementIds = orders.stream().map(OrderEntity::getRequirementId).collect(Collectors.toList());
        Map<String, RequirementEntity> requirementMap = requirementIds.isEmpty()
                ? Collections.emptyMap()
                : requirementMapper.selectBatchIds(requirementIds).stream()
                .collect(Collectors.toMap(RequirementEntity::getId, item -> item, (a, b) -> a));

        List<String> teacherIds = orders.stream().map(OrderEntity::getTeacherId).distinct().collect(Collectors.toList());
        Map<String, TeacherInfoEntity> teacherMap = teacherIds.isEmpty()
                ? Collections.emptyMap()
                : teacherInfoMapper.selectBatchIds(teacherIds).stream()
                .collect(Collectors.toMap(TeacherInfoEntity::getId, item -> item, (a, b) -> a));
        List<String> teacherUserIds = teacherMap.values().stream().map(TeacherInfoEntity::getUserId).collect(Collectors.toList());
        List<String> parentUserIds = orders.stream().map(OrderEntity::getParentId).distinct().collect(Collectors.toList());
        List<String> allUserIds = new ArrayList<>();
        allUserIds.addAll(teacherUserIds);
        allUserIds.addAll(parentUserIds);
        Map<String, UserEntity> userMap = allUserIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(allUserIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, item -> item, (a, b) -> a));

        return orders.stream().map(order -> {
            OrderListItemVO vo = new OrderListItemVO();
            RequirementEntity requirement = requirementMap.get(order.getRequirementId());
            TeacherInfoEntity teacher = teacherMap.get(order.getTeacherId());
            UserEntity teacherUser = teacher == null ? null : userMap.get(teacher.getUserId());
            UserEntity parentUser = userMap.get(order.getParentId());

            vo.setId(order.getId());
            vo.setOrderNumber(order.getOrderNumber());
            vo.setRequirementId(order.getRequirementId());
            vo.setOrderStatus(order.getOrderStatus());
            vo.setOrderStatusText(statusText(order.getOrderStatus()));
            vo.setPayStatus(resolvePayStatus(order.getOrderStatus()));
            vo.setPayStatusText(resolvePayStatusText(resolvePayStatus(order.getOrderStatus())));
            vo.setOrderAmount(order.getOrderAmount());
            vo.setServiceFee(calcServiceFee(order.getOrderAmount()));
            vo.setLessonCount(1);
            vo.setUnitPrice(order.getOrderAmount());
            vo.setSubjectName(requirement == null ? "-" : requirement.getRequirementTitle());
            vo.setCounterpartyName(loginUser.getUserType() != null && loginUser.getUserType() == 0
                    ? (teacherUser == null ? "Teacher" : teacherUser.getUserName())
                    : (parentUser == null ? "Parent" : parentUser.getUserName()));
            vo.setOrderStartTime(order.getOrderStartTime());
            vo.setOrderEndTime(order.getOrderEndTime());
            vo.setOrderRemark(order.getOrderRemark());
            vo.setOrderAuditStatus(order.getOrderAuditStatus());
            vo.setCanReview(order.getOrderStatus() != null && order.getOrderStatus() == OrderStatusEnum.FINISHED.getCode());
            vo.setCanDispute(order.getOrderStatus() != null && order.getOrderStatus() != OrderStatusEnum.FINISHED.getCode());
            vo.setCreateTime(order.getCreateTime());
            vo.setUpdateTime(order.getUpdateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    private boolean matchKeyword(OrderListItemVO item, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String text = keyword.trim();
        return (item.getOrderNumber() != null && item.getOrderNumber().contains(text))
                || (item.getCounterpartyName() != null && item.getCounterpartyName().contains(text))
                || (item.getSubjectName() != null && item.getSubjectName().contains(text));
    }

    private BigDecimal calcServiceFee(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(new BigDecimal("0.05")).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private Integer resolvePayStatus(Integer orderStatus) {
        if (orderStatus == null) {
            return 0;
        }
        return orderStatus == OrderStatusEnum.WAITING_CONFIRM.getCode() ? 0 : 1;
    }

    private String resolvePayStatusText(Integer payStatus) {
        return payStatus != null && payStatus == 1 ? "已支付" : "待支付";
    }

    private String statusText(Integer orderStatus) {
        if (orderStatus == null) {
            return "未知";
        }
        if (orderStatus == OrderStatusEnum.WAITING_CONFIRM.getCode()) {
            return "待确认";
        }
        if (orderStatus == OrderStatusEnum.IN_PROGRESS.getCode()) {
            return "进行中";
        }
        if (orderStatus == OrderStatusEnum.FINISHED.getCode()) {
            return "已完成";
        }
        if (orderStatus == OrderStatusEnum.CANCELED.getCode()) {
            return "已取消";
        }
        return "未知";
    }

    private String resolveParentStageByOrder(Integer orderStatus) {
        if (orderStatus == null) {
            return "PUBLISHED_WAITING";
        }
        if (orderStatus == OrderStatusEnum.WAITING_CONFIRM.getCode()) {
            return "MATCHED";
        }
        if (orderStatus == OrderStatusEnum.IN_PROGRESS.getCode()) {
            return "IN_SERVICE";
        }
        if (orderStatus == OrderStatusEnum.FINISHED.getCode()) {
            return "DONE";
        }
        return "CLOSED";
    }

    private String resolveParentStageText(String stage) {
        if ("PUBLISHED_WAITING".equals(stage)) {
            return "等待接单";
        }
        if ("MATCHED".equals(stage)) {
            return "已被承接";
        }
        if ("IN_SERVICE".equals(stage)) {
            return "服务中";
        }
        if ("DONE".equals(stage)) {
            return "已完成";
        }
        if ("CLOSED".equals(stage)) {
            return "已关闭";
        }
        return "等待接单";
    }

    private Integer resolveRequirementStatusByOrder(Integer orderStatus) {
        if (orderStatus == null) {
            return RequirementStatusEnum.WAITING.getCode();
        }
        if (orderStatus == OrderStatusEnum.FINISHED.getCode()) {
            return RequirementStatusEnum.FINISHED.getCode();
        }
        if (orderStatus == OrderStatusEnum.CANCELED.getCode()) {
            return RequirementStatusEnum.CANCELED.getCode();
        }
        return RequirementStatusEnum.RECEIVED.getCode();
    }

    private String toChineseMissingFields(List<String> missingFields) {
        if (missingFields == null || missingFields.isEmpty()) {
            return "Please complete basic profile";
        }
        return missingFields.stream()
                .map(this::mapFieldLabel)
                .distinct()
                .collect(Collectors.joining(","));
    }

    private String mapFieldLabel(String fieldKey) {
        if ("nickName".equals(fieldKey)) return "昵称";
        if ("avatarUrl".equals(fieldKey)) return "头像";
        if ("teacherIdentity".equals(fieldKey)) return "教员身份";
        if ("teacherTutoringMethod".equals(fieldKey)) return "授课方式";
        if ("teacherSchool".equals(fieldKey)) return "学校";
        if ("teacherMajor".equals(fieldKey)) return "专业";
        if ("teacherEducation".equals(fieldKey)) return "学历";
        return "资料字段";
    }

    private List<String> resolveBlockingMissingFields(UserProfileCompletenessVO completeness) {
        if (completeness == null || completeness.getMissingFields() == null || completeness.getMissingFields().isEmpty()) {
            return Collections.emptyList();
        }
        return completeness.getMissingFields().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(field -> !field.isEmpty())
                .filter(field -> !NON_BLOCKING_PROFILE_FIELDS.contains(field))
                .collect(Collectors.toList());
    }

    private Map<String, String> buildUserNameMap(OrderEntity order) {
        Map<String, String> map = new HashMap<>();
        if (order == null) {
            return map;
        }
        UserEntity parent = userMapper.selectById(order.getParentId());
        if (parent != null) {
            map.put(order.getParentId(), parent.getUserName());
        }
        TeacherInfoEntity teacher = teacherInfoMapper.selectById(order.getTeacherId());
        if (teacher != null) {
            UserEntity teacherUser = userMapper.selectById(teacher.getUserId());
            if (teacherUser != null) {
                map.put(order.getTeacherId(), teacherUser.getUserName());
            }
        }
        return map;
    }

    private void ensureOrderPermission(LoginUser loginUser, OrderEntity order) {
        if (loginUser.isAdmin()) {
            return;
        }
        if (loginUser.getUserType() != null && loginUser.getUserType() == 0) {
            if (!loginUser.getId().equals(order.getParentId())) {
                    throw new BusinessException("No permission to operate this order");
                }
            return;
        }
        TeacherInfoEntity teacher = teacherInfoMapper.selectOne(new LambdaQueryWrapper<TeacherInfoEntity>()
                .eq(TeacherInfoEntity::getUserId, loginUser.getId())
                .eq(TeacherInfoEntity::getTeacherDeleteStatus, 0)
                .last("limit 1"));
        if (teacher == null || !teacher.getId().equals(order.getTeacherId())) {
                    throw new BusinessException("No permission to operate this order");
                }
    }
}




