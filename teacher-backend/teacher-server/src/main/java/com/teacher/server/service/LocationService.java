package com.teacher.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teacher.common.exception.BusinessException;
import com.teacher.pojo.vo.LocationReverseVO;
import com.teacher.server.config.AmapProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class LocationService {
    private final AmapProperties amapProperties;
    private final ObjectMapper objectMapper;
    private volatile HttpClient httpClient;

    public LocationService(AmapProperties amapProperties, ObjectMapper objectMapper) {
        this.amapProperties = amapProperties;
        this.objectMapper = objectMapper;
    }

    public LocationReverseVO reverse(BigDecimal latitude, BigDecimal longitude, String requestKey) {
        String key = resolveAmapKey(requestKey);
        if (key.isEmpty()) {
            throw new BusinessException("未配置高德地图 AMAP_WEB_KEY");
        }
        try {
            String location = longitude.stripTrailingZeros().toPlainString() + "," + latitude.stripTrailingZeros().toPlainString();
            String url = "https://restapi.amap.com/v3/geocode/regeo?location=" + location + "&key=" + key;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .header("User-Agent", "teacher-server/1.0")
                    .GET()
                    .build();
            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException("逆地理服务异常，HTTP状态码: " + response.statusCode());
            }

            JsonNode node = objectMapper.readTree(response.body());
            String status = node.path("status").asText("");
            if (!"1".equals(status)) {
                String info = node.path("info").asText("unknown");
                String infocode = node.path("infocode").asText("unknown");
                throw new BusinessException("逆地理解析失败(status=" + status + ", infocode=" + infocode + "): " + info);
            }

            JsonNode result = node.path("regeocode");
            JsonNode component = result.path("addressComponent");
            String province = component.path("province").asText("");
            String city = resolveAmapCity(component.path("city"), province);
            return new LocationReverseVO(
                    result.path("formatted_address").asText(""),
                    province,
                    city,
                    component.path("district").asText("")
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("逆地理服务暂不可用: " + ex.getClass().getSimpleName() + ", " + ex.getMessage());
        }
    }

    private String resolveAmapKey(String requestKey) {
        String fromRequest = String.valueOf(requestKey == null ? "" : requestKey).trim();
        if (!fromRequest.isEmpty()) {
            return fromRequest;
        }
        return String.valueOf(amapProperties.getKey() == null ? "" : amapProperties.getKey()).trim();
    }

    private String resolveAmapCity(JsonNode cityNode, String province) {
        if (cityNode == null || cityNode.isMissingNode() || cityNode.isNull()) {
            return String.valueOf(province == null ? "" : province);
        }
        if (cityNode.isTextual()) {
            String cityText = cityNode.asText("");
            return cityText.isEmpty() ? String.valueOf(province == null ? "" : province) : cityText;
        }
        if (cityNode.isArray()) {
            if (cityNode.size() <= 0) {
                return String.valueOf(province == null ? "" : province);
            }
            JsonNode first = cityNode.get(0);
            if (first == null || first.isNull()) {
                return String.valueOf(province == null ? "" : province);
            }
            String cityText = first.asText("");
            return cityText.isEmpty() ? String.valueOf(province == null ? "" : province) : cityText;
        }
        return String.valueOf(province == null ? "" : province);
    }

    private HttpClient getHttpClient() {
        if (httpClient != null) {
            return httpClient;
        }
        synchronized (this) {
            if (httpClient == null) {
                try {
                    httpClient = HttpClient.newBuilder().build();
                } catch (RuntimeException ex) {
                    throw new BusinessException("逆地理服务暂不可用: HTTP客户端初始化异常");
                }
            }
            return httpClient;
        }
    }
}
