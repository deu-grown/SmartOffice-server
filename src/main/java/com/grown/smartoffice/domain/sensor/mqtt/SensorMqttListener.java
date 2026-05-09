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
     * MQTT 메시지 처리 (센서 데이터)
     * 토픽 예시: smartoffice/zones/1/devices/10/sensor
     * 페이로드 예시: {"value": 24.5, "unit": "°C", "type": "TEMPERATURE", "timestamp": "2026-04-02T14:30:00"}
     */
    public void onMessage(String topic, String payload) {
        try {
            log.debug("[MQTT] 센서 데이터 수신 — topic={}, payload={}", topic, payload);
            
            // 토픽 구조: smartoffice/zones/{zoneId}/devices/{deviceId}/sensor
            String[] parts = topic.split("/");
            Long zoneId = null;
            Long deviceId = null;
            
            if (parts.length >= 5 && parts[1].equals("zones") && parts[3].equals("devices")) {
                zoneId = Long.parseLong(parts[2]);
                deviceId = Long.parseLong(parts[4]);
            }

            if (zoneId == null || deviceId == null) {
                log.warn("[MQTT] 토픽 형식이 올바르지 않아 정보를 추출할 수 없습니다: {}", topic);
                return;
            }

            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            
            SensorLogRequest request = SensorLogRequest.builder()
                    .zoneId(zoneId)
                    .deviceId(deviceId)
                    .sensorType((String) data.getOrDefault("type", data.get("sensorType")))
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
