package org.example.orderservice.kafka;

import org.example.orderservice.clients.InventoryClient;
import org.example.orderservice.models.Order;
import org.example.orderservice.models.OrderStatus;
import org.example.orderservice.repositories.InventoryRollbackTaskRepository;
import org.example.orderservice.repositories.OrderAuditLogRepository;
import org.example.orderservice.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.mockito.Mockito.*;

class KafkaOrderConsumerTest {

    @Mock private OrderRepository orderRepository;
    @Mock private InventoryClient inventoryClient;
    @Mock private OrderAuditLogRepository auditLogRepository;
    @Mock private InventoryRollbackTaskRepository rollbackTaskRepository;

    @InjectMocks private KafkaOrderConsumer consumer;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldUpdateOrderToShippedIfStatusIsPlaced() {
        Order order = new Order();
        order.setId(1001L);
        order.setStatus(OrderStatus.PLACED);

        when(orderRepository.findById(1001L)).thenReturn(Optional.of(order));

        consumer.handlePaymentSuccessEvent("1001");

        verify(orderRepository).save(order);
        assert order.getStatus() == OrderStatus.SHIPPED;
    }

    @Test
    void shouldSkipIfOrderNotInPlacedState() {
        Order order = new Order();
        order.setId(1002L);
        order.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(1002L)).thenReturn(Optional.of(order));

        consumer.handlePaymentSuccessEvent("1002");

        verify(orderRepository, never()).save(order);
    }

    @Test
    void shouldLogErrorIfOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        consumer.handlePaymentSuccessEvent("999");

        verify(orderRepository).findById(999L);
    }

    @Test
    void shouldHandleInvalidOrderIdFormatGracefully() {
        consumer.handlePaymentSuccessEvent("not-a-number");
        // Should not throw
    }
}
