package org.example.orderservice.configs.kafka;


import org.example.orderservice.models.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import org.springframework.retry.support.RetryTemplate;

@Service
public class KafkaPublisher {

    private static final Logger logger = LoggerFactory.getLogger(KafkaPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String orderPlacedTopic;
    private final String orderCancelledTopic;
    private final RetryTemplate kafkaRetryTemplate;

    public KafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                          @Value("${order.kafka.topic.placed}") String orderPlacedTopic,
                          @Value("${order.kafka.topic.cancelled}") String orderCancelledTopic,
                          RetryTemplate kafkaRetryTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderPlacedTopic = orderPlacedTopic;
        this.orderCancelledTopic = orderCancelledTopic;
        this.kafkaRetryTemplate = kafkaRetryTemplate;
    }

    public void publishOrderPlaced(Order order) {
        kafkaRetryTemplate.execute(context -> {
            kafkaTemplate.send(orderPlacedTopic, order.getId().toString(), order);
            logger.info("📦 Order placed event sent for order id {}", order.getId());
            return null;
        }, context -> {
            logger.error("❌ Failed to publish order placed event for order id {} after retries", order.getId());
            return null;
        });
    }

    public void publishOrderCancelled(Order order) {
        kafkaRetryTemplate.execute(context -> {
            kafkaTemplate.send(orderCancelledTopic, order.getId().toString(), order);
            logger.info("❌ Order cancelled event sent for order id {}", order.getId());
            return null;
        }, context -> {
            logger.error("❌ Failed to publish order cancelled event for order id {} after retries", order.getId());
            return null;
        });
    }
}


