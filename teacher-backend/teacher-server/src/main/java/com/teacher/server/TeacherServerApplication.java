package com.teacher.server;

import com.teacher.server.config.AmapProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@SpringBootApplication(scanBasePackages = "com.teacher")
@ConfigurationPropertiesScan(basePackages = "com.teacher.server.config")
public class TeacherServerApplication {
    private static final Logger log = LoggerFactory.getLogger(TeacherServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TeacherServerApplication.class, args);
    }

    @Bean
    public ApplicationRunner amapKeyHealthCheck(AmapProperties amapProperties) {
        return args -> {
            String key = amapProperties == null ? "" : amapProperties.getKey();
            boolean configured = StringUtils.hasText(key);
            if (configured) {
                log.info("[StartupCheck] AMAP_WEB_KEY configured: true");
            } else {
                log.warn("[StartupCheck] AMAP_WEB_KEY configured: false, reverse geocode will fallback");
            }
        };
    }
}
