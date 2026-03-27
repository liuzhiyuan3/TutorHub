package com.teacher.server.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.teacher.common.exception.BusinessException;
import com.teacher.common.util.IdUtil;
import com.teacher.pojo.vo.FileUploadVO;
import com.teacher.server.config.OssProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.Set;

@Service
public class OssStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private final OssProperties ossProperties;

    public OssStorageService(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    public FileUploadVO uploadImage(MultipartFile file, String biz) {
        validateOssConfig();
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传失败：文件不能为空");
        }
        long maxBytes = (long) ossProperties.getMaxSizeMb() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessException("上传失败：文件过大");
        }
        String ext = resolveExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException("上传失败：仅支持 jpg/png/webp");
        }
        String safeBiz = sanitizeBiz(biz);
        LocalDate now = LocalDate.now();
        String key = String.format(
                "%s/%s/%d/%02d/%02d/%s.%s",
                trimSlash(ossProperties.getDirPrefix()),
                safeBiz,
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                IdUtil.uuid32(),
                ext
        );

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentTypeByExt(ext));
        metadata.setContentLength(file.getSize());
        OSS ossClient = null;
        try (InputStream inputStream = file.getInputStream()) {
            ossClient = new OSSClientBuilder().build(
                    normalizeEndpoint(ossProperties.getEndpoint()),
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret()
            );
            ossClient.putObject(ossProperties.getBucket(), key, inputStream, metadata);
        } catch (IOException ex) {
            throw new BusinessException("上传失败：文件读取异常");
        } catch (Exception ex) {
            throw new BusinessException("上传失败：OSS服务异常");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        String baseUrl = trimSlash(ossProperties.getBaseUrl());
        if (baseUrl.isBlank()) {
            baseUrl = String.format("https://%s.%s", ossProperties.getBucket(), normalizeEndpoint(ossProperties.getEndpoint()));
        }
        return new FileUploadVO(baseUrl + "/" + key, key);
    }

    private void validateOssConfig() {
        List<String> missing = new ArrayList<>();
        if (isBlank(ossProperties.getEndpoint())) {
            missing.add("OSS_ENDPOINT(app.oss.endpoint)");
        }
        if (isBlank(ossProperties.getAccessKeyId())) {
            missing.add("OSS_ACCESS_KEY_ID(app.oss.access-key-id)");
        }
        if (isBlank(ossProperties.getAccessKeySecret())) {
            missing.add("OSS_ACCESS_KEY_SECRET(app.oss.access-key-secret)");
        }
        if (isBlank(ossProperties.getBucket())) {
            missing.add("OSS_BUCKET(app.oss.bucket)");
        }
        if (!missing.isEmpty()) {
            throw new BusinessException("上传失败：OSS配置缺失 -> " + String.join(", ", missing));
        }
    }

    private String sanitizeBiz(String biz) {
        if (biz == null || biz.isBlank()) {
            return "common";
        }
        String normalized = biz.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\-_/]", "");
        return normalized.isBlank() ? "common" : normalized;
    }

    private String resolveExtension(String originalName) {
        if (originalName == null) {
            return "";
        }
        int index = originalName.lastIndexOf('.');
        if (index < 0 || index == originalName.length() - 1) {
            return "";
        }
        return originalName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String contentTypeByExt(String ext) {
        if ("png".equals(ext)) {
            return "image/png";
        }
        if ("webp".equals(ext)) {
            return "image/webp";
        }
        return "image/jpeg";
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    private String trimSlash(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("/+$", "");
    }

    private String normalizeEndpoint(String endpoint) {
        if (endpoint == null) {
            return "";
        }
        String normalized = endpoint.trim()
                .replaceFirst("^https?://", "")
                .replaceAll("/+$", "");
        return normalized;
    }
}
