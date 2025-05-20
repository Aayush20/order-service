package org.example.orderservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.orderservice.clients.InventoryClient;
import org.example.orderservice.dtos.RollbackStockRequestDto;
import org.example.orderservice.models.*;
import org.example.orderservice.repositories.InventoryRollbackTaskRepository;
import org.example.orderservice.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

class InventoryRollbackServiceTest {

    @Mock private InventoryClient inventoryClient;
    @Mock private OrderRepository orderRepository;
    @Mock private InventoryRollbackTaskRepository rollbackTaskRepository;

    @InjectMocks private InventoryRollbackService rollbackService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        rollbackService = new InventoryRollbackService(inventoryClient, orderRepository, rollbackTaskRepository);
    }

    @Test
    void shouldRollbackInventorySuccessfully() {
        Order order = new Order();
        order.setId(101L);
        order.addOrderItem(new OrderItem(1L, 2));

        rollbackService.rollbackInventory(order, "test reason");

        verify(inventoryClient).rollbackStock(any(RollbackStockRequestDto.class));
        verify(rollbackTaskRepository, never()).save(any());
    }

    @Test
    void shouldQueueRetryIfRollbackFails() throws Exception {
        Order order = new Order();
        order.setId(202L);
        order.addOrderItem(new OrderItem(1L, 3));

        doThrow(new RuntimeException("Inventory down")).when(inventoryClient).rollbackStock(any());

        rollbackService.rollbackInventory(order, "failure test");

        verify(rollbackTaskRepository).save(any());
    }

    @Test
    void shouldCancelAndRollbackOnPaymentFailure() {
        Order order = new Order();
        order.setId(303L);
        order.setStatus(OrderStatus.PLACED);
        order.addOrderItem(new OrderItem(2L, 1));

        when(orderRepository.findById(303L)).thenReturn(Optional.of(order));

        rollbackService.rollbackInventoryForFailedPayment("303", "user123", "payment failed");

        verify(orderRepository).save(order);
        verify(inventoryClient).rollbackStock(any());
    }

    @Test
    void shouldSkipRollbackIfOrderNotPlaced() {
        Order order = new Order();
        order.setId(404L);
        order.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(404L)).thenReturn(Optional.of(order));

        rollbackService.rollbackInventoryForFailedPayment("404", "user456", "skip rollback");

        verify(inventoryClient, never()).rollbackStock(any());
    }

    @Test
    void shouldRetryAllFailedRollbackTasks() throws Exception {
        InventoryRollbackTask task = new InventoryRollbackTask();
        task.setOrderId(999L);
        RollbackStockRequestDto dto = new RollbackStockRequestDto(
                List.of(new RollbackStockRequestDto.ProductRollbackEntry(1L, 5)), "retry test"
        );
        task.setPayload(objectMapper.writeValueAsString(dto));
        task.setRetryCount(0);
        task.setLastTriedAt(Instant.now());

        when(rollbackTaskRepository.findAll()).thenReturn(List.of(task));

        rollbackService.retryFailedTasks();

        verify(inventoryClient).rollbackStock(any());
        verify(rollbackTaskRepository).delete(task);
    }
}
