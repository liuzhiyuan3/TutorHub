package com.teacher.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teacher.common.exception.BusinessException;
import com.teacher.pojo.vo.LocationReverseVO;
import com.teacher.server.config.QQMapProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class LocationService {
    private final QQMapProperties qqMapProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().build();

    public LocationService(QQMapProperties qqMapProperties, ObjectMapper objectMapper) {
        this.qqMapProperties = qqMapProperties;
        this.objectMapper = objectMapper;
    }

    public LocationReverseVO reverse(BigDecimal latitude, BigDecimal longitude) {
        String key = String.valueOf(qqMapProperties.getKey() == null ? "" : qqMapProperties.getKey()).trim();
        if (key.isEmpty()) {
            throw new BusinessException("未配置腾讯地图 QQ_MAP_KEY");
        }
        try {
            String location = latitude.stripTrailingZeros().toPlainString() + "," + longitude.stripTrailingZeros().toPlainString();
            String url = "https://apis.map.qq.com/ws/geocoder/v1/?location="
                    + URLEncoder.encode(location, StandardCharsets.UTF_8)
                    + "&key=" + URLEncoder.encode(key, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException("逆地理服务异常，HTTP状态码: " + response.statusCode());
            }

            JsonNode node = objectMapper.readTree(response.body());
            int status = node.path("status").asInt(-1);
            if (status != 0) {
                String message = node.path("message").asText("unknown");
                throw new BusinessException("逆地理解析失败(status=" + status + "): " + message);
            }

            JsonNode result = node.path("result");
            JsonNode component = result.path("address_component");
            return new LocationReverseVO(
                    result.path("address").asText(""),
                    component.path("province").asText(""),
                    component.path("city").asText(""),
                    component.path("district").asText("")
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("逆地理服务暂不可用: " + ex.getClass().getSimpleName());
        }
    }
}
