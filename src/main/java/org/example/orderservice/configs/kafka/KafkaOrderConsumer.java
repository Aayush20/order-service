package org.example.orderservice.configs.kafka;

import org.example.orderservice.models.Order;
import org.example.orderservice.models.OrderStatus;
import org.example.orderservice.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class KafkaOrderConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaOrderConsumer.class);
    private final OrderRepository orderRepository;

    public KafkaOrderConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    @KafkaListener(topics = "payment_success", groupId = "order-service")
    public void handlePaymentSuccessEvent(String orderIdStr) {
        try {
            Long orderId = Long.parseLong(orderIdStr);
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found for ID: " + orderId));

            if (order.getStatus() == OrderStatus.PLACED) {
                order.setStatus(OrderStatus.SHIPPED);
                orderRepository.save(order);
                logger.info("✅ Order {} marked as SHIPPED after payment confirmation.", orderId);
            } else {
                logger.warn("Order {} is not in PLACED state, skipping SHIPPED update.", orderId);
            }
        } catch (Exception ex) {
            logger.error("Error processing payment success event: {}", ex.getMessage());
        }
    }
}
