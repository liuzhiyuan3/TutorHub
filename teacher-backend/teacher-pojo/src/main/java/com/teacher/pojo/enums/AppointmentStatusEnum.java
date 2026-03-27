package com.teacher.pojo.enums;

public enum AppointmentStatusEnum {
    WAITING_CONFIRM(0),
    CONFIRMED(1),
    REJECTED(2),
    CANCELED(3);

    private final int code;

    AppointmentStatusEnum(int code) {
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
        if (from == WAITING_CONFIRM.code && (to == CONFIRMED.code || to == REJECTED.code || to == CANCELED.code)) {
            return true;
        }
        return false;
    }
}
