package com.teacher.pojo.enums;

public enum AuditStatusEnum {
    PENDING(0),
    APPROVED(1),
    REJECTED(2);

    private final int code;

    AuditStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static boolean isValid(Integer code) {
        if (code == null) {
            return false;
        }
        for (AuditStatusEnum value : values()) {
            if (value.code == code) {
                return true;
            }
        }
        return false;
    }
}
