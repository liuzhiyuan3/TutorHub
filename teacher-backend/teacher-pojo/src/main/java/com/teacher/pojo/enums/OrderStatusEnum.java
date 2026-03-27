package com.teacher.pojo.enums;

public enum OrderStatusEnum {
    WAITING_CONFIRM(0),
    IN_PROGRESS(1),
    FINISHED(2),
    CANCELED(3);

    private final int code;

    OrderStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static boolean canTransit(Integer from, Integer to) {
        if (from == null || to == null) {
            return false;
        }
        if (from.equals(to)) {
            return true;
        }
        if (from == WAITING_CONFIRM.code && (to == IN_PROGRESS.code || to == CANCELED.code)) {
            return true;
        }
        if (from == IN_PROGRESS.code && (to == FINISHED.code || to == CANCELED.code)) {
            return true;
        }
        return false;
    }
}
