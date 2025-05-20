package org.example.orderservice.configs;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "order")
public class ApplicationProperties {

    @NotBlank
    private String kafkaTopicPlaced;

    @NotBlank
    private String kafkaTopicCancelled;

    public String getKafkaTopicPlaced() {
        return kafkaTopicPlaced;
    }

    public void setKafkaTopicPlaced(String kafkaTopicPlaced) {
        this.kafkaTopicPlaced = kafkaTopicPlaced;
    }

    public String getKafkaTopicCancelled() {
        return kafkaTopicCancelled;
    }

    public void setKafkaTopicCancelled(String kafkaTopicCancelled) {
        this.kafkaTopicCancelled = kafkaTopicCancelled;
    }
}
