package com.teacher.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.amap")
public class AmapProperties {
    private String key;
}

