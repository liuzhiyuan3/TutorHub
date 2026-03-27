package com.teacher.pojo.enums;

public enum RequirementStatusEnum {
    WAITING(0),
    RECEIVED(1),
    FINISHED(2),
    CANCELED(3);

    private final int code;

    RequirementStatusEnum(int code) {
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
        if (from == WAITING.code && (to == RECEIVED.code || to == CANCELED.code)) {
            return true;
        }
        if (from == RECEIVED.code && (to == FINISHED.code || to == CANCELED.code)) {
            return true;
        }
        return false;
    }
}
