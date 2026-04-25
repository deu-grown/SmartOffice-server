package com.grown.smartoffice.domain.accesslog.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.accesslog.dto.TagEventRequest;
import com.grown.smartoffice.domain.accesslog.service.AccessLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true")
@RequiredArgsConstructor
public class AccessLogMqttListener {

    private final AccessLogService accessLogService;
    private final ObjectMapper objectMapper;

    public void onMessage(String topic, String payload) {
        try {
            TagEventRequest request = objectMapper.readValue(payload, TagEventRequest.class);
            accessLogService.processTag(request);
            log.debug("[MQTT] 태그 처리 완료 — topic={}", topic);
        } catch (Exception e) {
            log.error("[MQTT] 메시지 처리 실패 — topic={}, payload={}, error={}",
                    topic, payload, e.getMessage());
        }
    }
}
