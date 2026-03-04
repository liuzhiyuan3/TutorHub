package com.teacher.server.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.teacher.server.mapper")
public class MapperScanConfig {
}
