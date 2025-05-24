package org.example.orderservice.integration;

import org.example.orderservice.integration.IntegrationTestConfig;
import org.example.orderservice.kafka.PaymentFailedEvent;
import org.example.orderservice.models.Order;
import org.example.orderservice.models.OrderStatus;
import org.example.orderservice.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderRollbackIntegrationTest extends IntegrationTestConfig {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldCancelOrderOnPaymentFailure() throws InterruptedException {
        // Setup: Create and save a PLACED order
        Order order = new Order();
        order.setUserId("user123");
        order.setStatus(OrderStatus.PLACED);
        order = orderRepository.save(order);

        // Send Kafka event
        PaymentFailedEvent event = new PaymentFailedEvent(
                String.valueOf(order.getId()), "user123", "stripe", "Insufficient funds", System.currentTimeMillis()
        );
        kafkaTemplate.send("payment.failed", event);

        Thread.sleep(3000); // wait for listener to process

        Optional<Order> updated = orderRepository.findById(order.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
}
