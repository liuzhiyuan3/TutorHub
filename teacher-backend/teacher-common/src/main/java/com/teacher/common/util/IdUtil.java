package com.teacher.common.util;

import java.util.UUID;

public final class IdUtil {
    private IdUtil() {
    }

    public static String uuid32() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
