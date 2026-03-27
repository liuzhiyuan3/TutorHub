package com.teacher.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.qq-map")
public class QQMapProperties {
    private String key;
}

