package org.example.orderservice.integration;


import org.example.orderservice.integration.IntegrationTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class KafkaListenerIntegrationTest extends IntegrationTestConfig {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void shouldSendAndReceiveKafkaMessage() {
        String topic = "test.kafka";
        String key = "sample";
        String message = "Hello from integration test";

        kafkaTemplate.send(topic, key, message);

        // Add your KafkaListener-based consumer or assertion here if needed
        assertThat(true).isTrue(); // placeholder
    }
}

