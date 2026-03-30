package com.teacher.pojo.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusTransitionEnumTest {

    @Test
    void orderStatusTransitionsShouldFollowBusinessRules() {
        assertTrue(OrderStatusEnum.canTransit(OrderStatusEnum.WAITING_CONFIRM.getCode(), OrderStatusEnum.WAITING_CONFIRM.getCode()));
        assertTrue(OrderStatusEnum.canTransit(OrderStatusEnum.WAITING_CONFIRM.getCode(), OrderStatusEnum.IN_PROGRESS.getCode()));
        assertTrue(OrderStatusEnum.canTransit(OrderStatusEnum.WAITING_CONFIRM.getCode(), OrderStatusEnum.CANCELED.getCode()));
        assertTrue(OrderStatusEnum.canTransit(OrderStatusEnum.IN_PROGRESS.getCode(), OrderStatusEnum.FINISHED.getCode()));
        assertTrue(OrderStatusEnum.canTransit(OrderStatusEnum.IN_PROGRESS.getCode(), OrderStatusEnum.CANCELED.getCode()));

        assertFalse(OrderStatusEnum.canTransit(OrderStatusEnum.WAITING_CONFIRM.getCode(), OrderStatusEnum.FINISHED.getCode()));
        assertFalse(OrderStatusEnum.canTransit(OrderStatusEnum.FINISHED.getCode(), OrderStatusEnum.IN_PROGRESS.getCode()));
        assertFalse(OrderStatusEnum.canTransit(OrderStatusEnum.CANCELED.getCode(), OrderStatusEnum.IN_PROGRESS.getCode()));
        assertFalse(OrderStatusEnum.canTransit(null, OrderStatusEnum.WAITING_CONFIRM.getCode()));
        assertFalse(OrderStatusEnum.canTransit(OrderStatusEnum.WAITING_CONFIRM.getCode(), null));
    }

    @Test
    void requirementStatusTransitionsShouldFollowBusinessRules() {
        assertTrue(RequirementStatusEnum.canTransit(RequirementStatusEnum.WAITING.getCode(), RequirementStatusEnum.WAITING.getCode()));
        assertTrue(RequirementStatusEnum.canTransit(RequirementStatusEnum.WAITING.getCode(), RequirementStatusEnum.RECEIVED.getCode()));
        assertTrue(RequirementStatusEnum.canTransit(RequirementStatusEnum.WAITING.getCode(), RequirementStatusEnum.CANCELED.getCode()));
        assertTrue(RequirementStatusEnum.canTransit(RequirementStatusEnum.RECEIVED.getCode(), RequirementStatusEnum.FINISHED.getCode()));
        assertTrue(RequirementStatusEnum.canTransit(RequirementStatusEnum.RECEIVED.getCode(), RequirementStatusEnum.CANCELED.getCode()));

        assertFalse(RequirementStatusEnum.canTransit(RequirementStatusEnum.WAITING.getCode(), RequirementStatusEnum.FINISHED.getCode()));
        assertFalse(RequirementStatusEnum.canTransit(RequirementStatusEnum.FINISHED.getCode(), RequirementStatusEnum.RECEIVED.getCode()));
        assertFalse(RequirementStatusEnum.canTransit(RequirementStatusEnum.CANCELED.getCode(), RequirementStatusEnum.RECEIVED.getCode()));
        assertFalse(RequirementStatusEnum.canTransit(null, RequirementStatusEnum.WAITING.getCode()));
        assertFalse(RequirementStatusEnum.canTransit(RequirementStatusEnum.WAITING.getCode(), null));
    }
}
