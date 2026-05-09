package com.grown.smartoffice.domain.sensor.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.sensor.dto.SensorLogRequest;
import com.grown.smartoffice.domain.sensor.service.SensorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true")
@RequiredArgsConstructor
public class SensorMqttListener {

    private final SensorService sensorService;
    private final ObjectMapper objectMapper;

    /**
     * MQTT 센서 데이터 수신 처리
     * 토픽 구조: smartoffice/{zone_id}/{sensor_type}
     * 페이로드: {"deviceId": 10, "value": 24.5, "unit": "°C", "timestamp": "2026-04-02T14:30:00"}
     */
    public void onMessage(String topic, String payload) {
        try {
            log.debug("[MQTT] 센서 데이터 수신 — topic={}, payload={}", topic, payload);

            // 토픽 파싱: smartoffice/{zone_id}/{sensor_type}
            String[] parts = topic.split("/");
            if (parts.length < 3) {
                log.warn("[MQTT] 토픽 형식이 올바르지 않습니다: {}", topic);
                return;
            }

            Long zoneId = Long.parseLong(parts[1]);
            String sensorType = parts[2];

            Map<String, Object> data = objectMapper.readValue(payload, Map.class);

            Long deviceId = Long.parseLong(data.get("deviceId").toString());

            SensorLogRequest request = SensorLogRequest.builder()
                    .zoneId(zoneId)
                    .deviceId(deviceId)
                    .sensorType(sensorType)
                    .value(new BigDecimal(data.get("value").toString()))
                    .unit((String) data.get("unit"))
                    .timestamp(data.containsKey("timestamp") ?
                            LocalDateTime.parse(data.get("timestamp").toString()) : LocalDateTime.now())
                    .build();

            sensorService.recordLog(request);

        } catch (Exception e) {
            log.error("[MQTT] 센서 메시지 처리 실패 — topic={}, payload={}, error={}",
                    topic, payload, e.getMessage());
        }
    }
}
