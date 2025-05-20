package org.example.orderservice.kafka;

import org.example.orderservice.models.Order;
import org.example.orderservice.models.OrderItem;
import org.example.orderservice.models.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class KafkaPublisherTest {

    private KafkaPublisher kafkaPublisher;
    private KafkaTemplate<String, Object> kafkaTemplate;
    private RetryTemplate retryTemplate;

    @BeforeEach
    void setup() {
        kafkaTemplate = mock(KafkaTemplate.class);
        retryTemplate = RetryTemplate.builder().maxAttempts(1).fixedBackoff(10).build();

        kafkaPublisher = new KafkaPublisher(
                kafkaTemplate,
                "order.placed",
                "order.cancelled",
                retryTemplate
        );
    }

    @Test
    void shouldPublishOrderPlacedEvent() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId("user123");
        order.setStatus(OrderStatus.PLACED);
        order.setOrderItems(List.of(new OrderItem(1L, 2, "Item A", BigDecimal.valueOf(100), "INR")));

        kafkaPublisher.publishOrderPlaced(order);

        verify(kafkaTemplate).send("order.placed", "1", order);
    }

    @Test
    void shouldPublishOrderCancelledEvent() {
        Order order = new Order();
        order.setId(42L);
        order.setUserId("user456");
        order.setStatus(OrderStatus.CANCELLED);

        kafkaPublisher.publishOrderCancelled(order);

        verify(kafkaTemplate).send("order.cancelled", "42", order);
    }
}
