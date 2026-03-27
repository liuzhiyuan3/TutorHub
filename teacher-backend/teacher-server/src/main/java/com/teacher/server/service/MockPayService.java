package com.teacher.server.service;

import com.teacher.common.exception.BusinessException;
import com.teacher.common.security.LoginUser;
import com.teacher.pojo.entity.OrderEntity;
import com.teacher.pojo.entity.TeacherInfoEntity;
import com.teacher.pojo.enums.OrderStatusEnum;
import com.teacher.pojo.vo.MockPayCreateVO;
import com.teacher.pojo.vo.MockPayResultVO;
import com.teacher.server.mapper.OrderMapper;
import com.teacher.server.mapper.TeacherInfoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MockPayService {
    private static final long EXPIRE_MINUTES = 15;

    private final OrderMapper orderMapper;
    private final TeacherInfoMapper teacherInfoMapper;
    private final AuthService authService;
    private final OrderService orderService;

    private final Map<String, MockPayTokenMeta> tokenStore = new ConcurrentHashMap<>();

    public MockPayService(OrderMapper orderMapper, TeacherInfoMapper teacherInfoMapper,
                          AuthService authService, OrderService orderService) {
        this.orderMapper = orderMapper;
        this.teacherInfoMapper = teacherInfoMapper;
        this.authService = authService;
        this.orderService = orderService;
    }

    public MockPayCreateVO create(String orderId) {
        OrderEntity order = requireParentOrder(orderId);
        if (order.getOrderStatus() != null && order.getOrderStatus() != OrderStatusEnum.WAITING_CONFIRM.getCode()) {
            throw new BusinessException("当前订单状态不可发起支付");
        }
        String payToken = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(EXPIRE_MINUTES);
        tokenStore.put(order.getId(), new MockPayTokenMeta(payToken, expireTime));
        return new MockPayCreateVO(order.getId(), order.getOrderNumber(), order.getOrderAmount(), payToken, expireTime);
    }

    @Transactional(rollbackFor = Exception.class)
    public MockPayResultVO confirm(String orderId, String payToken) {
        OrderEntity order = requireParentOrder(orderId);
        if (order.getOrderStatus() != null && order.getOrderStatus() == OrderStatusEnum.IN_PROGRESS.getCode()) {
            return result(order, "订单已支付，无需重复确认");
        }
        if (order.getOrderStatus() != null && order.getOrderStatus() == OrderStatusEnum.CANCELED.getCode()) {
            throw new BusinessException("订单已取消，无法支付");
        }
        if (order.getOrderStatus() != null && order.getOrderStatus() == OrderStatusEnum.FINISHED.getCode()) {
            return result(order, "订单已完成，无需支付");
        }
        MockPayTokenMeta meta = tokenStore.get(order.getId());
        if (meta == null || !meta.payToken.equals(payToken)) {
            throw new BusinessException("支付令牌无效，请重新发起支付");
        }
        if (meta.expireTime.isBefore(LocalDateTime.now())) {
            tokenStore.remove(order.getId());
            throw new BusinessException("支付已超时，请重新发起支付");
        }
        order.setOrderStatus(OrderStatusEnum.IN_PROGRESS.getCode());
        order.setOrderRemark("模拟支付成功");
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        orderService.syncRequirementStatusForExternal(order);
        tokenStore.remove(order.getId());
        return result(order, "支付成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public MockPayResultVO cancel(String orderId, String reason) {
        OrderEntity order = requireParentOrder(orderId);
        if (order.getOrderStatus() != null && order.getOrderStatus() == OrderStatusEnum.CANCELED.getCode()) {
            return result(order, "订单已取消");
        }
        if (order.getOrderStatus() != null && order.getOrderStatus() == OrderStatusEnum.FINISHED.getCode()) {
            throw new BusinessException("已完成订单不可取消");
        }
        order.setOrderStatus(OrderStatusEnum.CANCELED.getCode());
        order.setOrderRemark((reason == null || reason.isBlank()) ? "模拟支付取消" : reason);
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        orderService.syncRequirementStatusForExternal(order);
        tokenStore.remove(order.getId());
        return result(order, "订单已取消");
    }

    private OrderEntity requireParentOrder(String orderId) {
        LoginUser loginUser = authService.currentLoginUser();
        if (loginUser.getUserType() == null || loginUser.getUserType() != 0) {
            throw new BusinessException("仅家长可操作支付");
        }
        OrderEntity order = orderMapper.selectById(orderId);
        if (order == null || order.getOrderDeleteStatus() == 1) {
            throw new BusinessException("订单不存在");
        }
        if (!loginUser.getId().equals(order.getParentId())) {
            throw new BusinessException("无权操作该订单");
        }
        TeacherInfoEntity teacher = teacherInfoMapper.selectById(order.getTeacherId());
        if (teacher == null) {
            throw new BusinessException("订单教员信息不存在");
        }
        return order;
    }

    private MockPayResultVO result(OrderEntity order, String message) {
        int payStatus = (order.getOrderStatus() != null && order.getOrderStatus() == OrderStatusEnum.WAITING_CONFIRM.getCode()) ? 0 : 1;
        String payStatusText = payStatus == 1 ? "已支付" : "待支付";
        return new MockPayResultVO(order.getId(), order.getOrderStatus(), statusText(order.getOrderStatus()), payStatus, payStatusText, message);
    }

    private String statusText(Integer orderStatus) {
        if (orderStatus == null) return "未知";
        if (orderStatus == OrderStatusEnum.WAITING_CONFIRM.getCode()) return "待确认";
        if (orderStatus == OrderStatusEnum.IN_PROGRESS.getCode()) return "进行中";
        if (orderStatus == OrderStatusEnum.FINISHED.getCode()) return "已完成";
        if (orderStatus == OrderStatusEnum.CANCELED.getCode()) return "已取消";
        return "未知";
    }

    private static class MockPayTokenMeta {
        private final String payToken;
        private final LocalDateTime expireTime;

        private MockPayTokenMeta(String payToken, LocalDateTime expireTime) {
            this.payToken = payToken;
            this.expireTime = expireTime;
        }
    }
}
