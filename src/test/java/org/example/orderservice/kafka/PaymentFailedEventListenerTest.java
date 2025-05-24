package org.example.orderservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.orderservice.models.InventoryRollbackTask;
import org.example.orderservice.models.Order;
import org.example.orderservice.models.OrderItem;
import org.example.orderservice.models.OrderStatus;
import org.example.orderservice.repositories.InventoryRollbackTaskRepository;
import org.example.orderservice.repositories.OrderRepository;
import org.example.orderservice.services.InventoryRollbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.*;

class PaymentFailedEventListenerTest {

    @Mock private InventoryRollbackService rollbackService;
    @Mock private OrderRepository orderRepository;
    @Mock private InventoryRollbackTaskRepository rollbackTaskRepository;

    @InjectMocks private PaymentFailedEventListener listener;

    @Captor private ArgumentCaptor<InventoryRollbackTask> rollbackTaskCaptor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldRollbackInventoryIfOrderIsPlaced() {
        Order order = new Order();
        order.setId(101L);
        order.setStatus(OrderStatus.PLACED);
        order.addOrderItem(new OrderItem(1L, 2));

        PaymentFailedEvent event = new PaymentFailedEvent("101", "user123", "stripe", "Insufficient funds", Instant.now().toEpochMilli());

        when(orderRepository.findById(101L)).thenReturn(Optional.of(order));

        listener.handlePaymentFailedEvent(event);

        verify(orderRepository).save(order);
        verify(rollbackService).rollbackInventory(order, "Insufficient funds");
    }

    @Test
    void shouldSkipRollbackIfOrderNotPlaced() {
        Order order = new Order();
        order.setId(102L);
        order.setStatus(OrderStatus.DELIVERED);

        PaymentFailedEvent event = new PaymentFailedEvent("102", "user123", "razorpay", "Already shipped", Instant.now().toEpochMilli());

        when(orderRepository.findById(102L)).thenReturn(Optional.of(order));

        listener.handlePaymentFailedEvent(event);

        verify(orderRepository, never()).save(any());
        verify(rollbackService, never()).rollbackInventory(any(), any());
    }

    @Test
    void shouldQueueRollbackTaskIfOrderNotFound() throws Exception {
        PaymentFailedEvent event = new PaymentFailedEvent("999", "ghost", "stripe", "Order not found", Instant.now().toEpochMilli());

        when(orderRepository.findById(999L)).thenThrow(new RuntimeException("Not found"));

        listener.handlePaymentFailedEvent(event);

        verify(rollbackTaskRepository).save(rollbackTaskCaptor.capture());
        InventoryRollbackTask task = rollbackTaskCaptor.getValue();

        assert task.getOrderId() == 999L;
        assert task.getRetryCount() == 0;
        assert task.getPayload().contains("ghost");
        assert task.getPayload().contains("stripe");
    }

    @Test
    void shouldHandleInvalidOrderIdGracefully() {
        PaymentFailedEvent event = new PaymentFailedEvent("abc123", "userX", "stripe", "Invalid ID", Instant.now().toEpochMilli());

        listener.handlePaymentFailedEvent(event);

        // ✅ fallback logic should store rollback task with orderId = -1
        verify(rollbackTaskRepository).save(any());
        verify(rollbackService, never()).rollbackInventory(any(), any());
    }

}
