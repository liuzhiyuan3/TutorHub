package com.teacher.server.service;

import com.teacher.server.config.OssProperties;
import org.springframework.stereotype.Service;

@Service
public class MediaUrlService {
    private final OssProperties ossProperties;

    public MediaUrlService(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    public String normalize(String rawUrl) {
        String value = sanitize(rawUrl);
        if (value.isBlank()) {
            return "";
        }
        if (value.startsWith("data:image/")) {
            return value;
        }
        if (value.startsWith("https://")) {
            return value;
        }
        if (value.startsWith("//")) {
            return "https:" + value;
        }
        if (value.startsWith("http://")) {
            return value;
        }

        String baseUrl = resolveBaseUrl();
        if (baseUrl.isBlank()) {
            return value;
        }
        if (value.startsWith("./")) {
            value = value.substring(2);
        }
        if (value.startsWith("/")) {
            return baseUrl + value;
        }
        return baseUrl + "/" + value;
    }

    private String resolveBaseUrl() {
        String configured = trimTailSlash(ossProperties.getBaseUrl());
        if (!configured.isBlank()) {
            return configured;
        }
        String bucket = sanitize(ossProperties.getBucket());
        String endpoint = normalizeEndpoint(ossProperties.getEndpoint());
        if (bucket.isBlank() || endpoint.isBlank()) {
            return "";
        }
        return "https://" + bucket + "." + endpoint;
    }

    private String sanitize(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().replace("\\", "/");
    }

    private String trimTailSlash(String text) {
        return sanitize(text).replaceAll("/+$", "");
    }

    private String normalizeEndpoint(String endpoint) {
        return sanitize(endpoint)
                .replaceFirst("^https?://", "")
                .replaceAll("/+$", "");
    }
}
