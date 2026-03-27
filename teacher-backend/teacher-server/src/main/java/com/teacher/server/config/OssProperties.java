package com.teacher.server.config;

import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "app.oss")
public class OssProperties {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucket;
    private String baseUrl;
    private String dirPrefix = "teacher";
    @Min(value = 1, message = "OSS上传大小限制必须大于0")
    private Integer maxSizeMb = 5;
}
