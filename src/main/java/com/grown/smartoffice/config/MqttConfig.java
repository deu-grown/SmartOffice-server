package com.grown.smartoffice.config;

import com.grown.smartoffice.domain.accesslog.mqtt.AccessLogMqttListener;
import com.grown.smartoffice.domain.sensor.mqtt.SensorMqttListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "mqtt.enabled", havingValue = "true")
@RequiredArgsConstructor
public class MqttConfig {

    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.client-id:smartoffice-server}")
    private String clientId;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.topic.access:smartoffice/+/access}")
    private String accessTopic;

    @Value("${mqtt.topic.sensor:smartoffice/+/sensor}")
    private String sensorTopic;

    private final AccessLogMqttListener accessLogMqttListener;
    private final SensorMqttListener sensorMqttListener;

    @Bean
    public MqttClient mqttClient() throws MqttException {
        MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(60);
        if (username != null && !username.isBlank()) {
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.warn("[MQTT] 연결 끊김: {}", cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload = new String(message.getPayload());
                if (isMatch(accessTopic, topic)) {
                    accessLogMqttListener.onMessage(topic, payload);
                } else if (isMatch(sensorTopic, topic)) {
                    sensorMqttListener.onMessage(topic, payload);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });

        client.connect(options);
        client.subscribe(new String[]{accessTopic, sensorTopic}, new int[]{1, 1});
        log.info("[MQTT] 연결 완료, 토픽 구독: {}, {}", accessTopic, sensorTopic);

        return client;
    }

    private boolean isMatch(String pattern, String topic) {
        // + 와일드카드(단일 레벨) 매칭 — smartoffice/+/access → smartoffice/room1/access
        String regex = pattern.replace("+", "[^/]+").replace("#", ".*");
        return topic.matches(regex);
    }
}
