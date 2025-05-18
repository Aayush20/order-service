package org.example.orderservice.jobs;



import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.orderservice.kafka.KafkaPublisher;
import org.example.orderservice.kafka.PaymentFailedEvent;
import org.example.orderservice.models.Order;
import org.example.orderservice.models.RetryDeadLetterLog;
import org.example.orderservice.repositories.OrderRepository;
import org.example.orderservice.repositories.RetryDeadLetterLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OrderRetryConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderRetryConsumer.class);

    private final KafkaPublisher publisher;
    private final OrderRepository orderRepository;
    private final RetryDeadLetterLogRepository deadLetterRepo;

    public OrderRetryConsumer(KafkaPublisher publisher,
                              OrderRepository orderRepository,
                              RetryDeadLetterLogRepository deadLetterRepo) {
        this.publisher = publisher;
        this.orderRepository = orderRepository;
        this.deadLetterRepo = deadLetterRepo;
    }

    @KafkaListener(topics = "${topic.order.retry}", groupId = "order-retry-consumer")
    public void retryFailedEvent(ConsumerRecord<String, Object> record) {
        String orderId = record.key();
        Object value = record.value();

        logger.info("🔁 Consuming retry event for orderId={}, value={}", orderId, value);

        try {
            if (value instanceof Order order) {
                publisher.publishOrderPlaced(order);
            } else if (value instanceof PaymentFailedEvent failedEvent) {
                // Not needed unless you publish these from order-service
                logger.warn("⚠️ Unexpected retry event type: {}", value.getClass().getName());
            } else {
                logger.warn("⚠️ Unknown object type in retry topic: {}", value.getClass().getName());
            }
        } catch (Exception ex) {
            logger.error("🔥 Retry failed again for orderId={}: {}", orderId, ex.getMessage());

            RetryDeadLetterLog log = new RetryDeadLetterLog();
            log.setTopic(record.topic());
            log.setKey(orderId);
            log.setPayload(value.toString());
            log.setErrorMessage(ex.getMessage());
            log.setCreatedAt(LocalDateTime.now());
            deadLetterRepo.save(log);
        }
    }
}

