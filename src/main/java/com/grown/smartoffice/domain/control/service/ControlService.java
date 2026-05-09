package com.grown.smartoffice.domain.control.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grown.smartoffice.domain.control.dto.*;
import com.grown.smartoffice.domain.control.entity.ControlCommand;
import com.grown.smartoffice.domain.control.entity.ControlStatus;
import com.grown.smartoffice.domain.control.repository.ControlCommandRepository;
import com.grown.smartoffice.domain.device.entity.Device;
import com.grown.smartoffice.domain.device.repository.DeviceRepository;
import com.grown.smartoffice.domain.zone.entity.Zone;
import com.grown.smartoffice.domain.zone.repository.ZoneRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ControlService {

    private final ControlCommandRepository controlCommandRepository;
    private final ZoneRepository zoneRepository;
    private final DeviceRepository deviceRepository;
    private final Optional<MqttClient> mqttClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public ControlResponse sendCommand(ControlRequest request) {
        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new CustomException(ErrorCode.ZONE_NOT_FOUND));

        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND));

        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("command", request.getCommand());
        payloadMap.put("value", request.getValue());
        payloadMap.put("deviceId", String.valueOf(device.getDevicesId()));

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payloadMap);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        ControlCommand controlCommand = ControlCommand.builder()
                .zone(zone)
                .device(device)
                .commandType(request.getCommand())
                .payload(payloadJson)
                .status(ControlStatus.PENDING)
                .build();

        ControlCommand saved = controlCommandRepository.save(controlCommand);

        // MQTT 메시지 발송
        mqttClient.ifPresent(client -> {
            try {
                String topic = String.format("smartoffice/%d/command", zone.getZoneId());
                MqttMessage message = new MqttMessage(payloadJson.getBytes());
                message.setQos(1);
                client.publish(topic, message);
                log.info("[MQTT] 제어 명령 발송 완료 — topic={}, payload={}", topic, payloadJson);
            } catch (Exception e) {
                log.error("[MQTT] 제어 명령 발송 실패 — {}", e.getMessage());
                // Optional: status를 FAILED로 변경? (트랜잭션 밖에서 처리 필요할 수도 있음)
            }
        });

        return new ControlResponse(saved.getId());
    }

    public ControlDetailResponse getControlDetail(Long id) {
        ControlCommand command = controlCommandRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTROL_NOT_FOUND));
        return new ControlDetailResponse(command);
    }

    public ControlHistoryResponse getHistory(Long zoneId, LocalDate startDate) {
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : LocalDateTime.now().minusDays(7);
        
        List<ControlHistoryItem> history = controlCommandRepository.findHistory(zoneId, start).stream()
                .map(ControlHistoryItem::new)
                .collect(Collectors.toList());

        Map<String, Object> query = new HashMap<>();
        query.put("zoneId", zoneId);
        query.put("startDate", startDate);

        return ControlHistoryResponse.builder()
                .searchQuery(query)
                .totalCount(history.size())
                .controlList(history)
                .build();
    }
}
