package org.example.orderservice.schedulers;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.example.orderservice.clients.InventoryClient;
import org.example.orderservice.dtos.RollbackStockRequestDto;
import org.example.orderservice.models.*;
import org.example.orderservice.repositories.OrderAuditLogRepository;
import org.example.orderservice.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderExpirySchedulerTest {

    private OrderRepository orderRepository;
    private OrderAuditLogRepository auditLogRepository;
    private InventoryClient inventoryClient;
    private OrderExpiryScheduler scheduler;

    @BeforeEach
    void setup() {
        orderRepository = mock(OrderRepository.class);
        auditLogRepository = mock(OrderAuditLogRepository.class);
        inventoryClient = mock(InventoryClient.class);

        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        scheduler = new OrderExpiryScheduler(orderRepository, auditLogRepository, inventoryClient, meterRegistry);
        scheduler.setExpiryMinutes(10); // inject manually
    }

    @Test
    void shouldExpireOldPlacedOrders() {
        // Setup mock order that is older than 10 mins
        Order oldOrder = new Order();
        oldOrder.setId(101L);
        oldOrder.setStatus(OrderStatus.PLACED);
        oldOrder.setOrderDate(Date.from(Instant.now().minusSeconds(900)));
        oldOrder.setUserId("user1");

        OrderItem item1 = new OrderItem(1L, 2);
        OrderItem item2 = new OrderItem(2L, 1);
        oldOrder.setOrderItems(Arrays.asList(item1, item2));

        when(orderRepository.findExpiredUnshippedOrders(any())).thenReturn(List.of(oldOrder));

        scheduler.expireOldOrders();

        // Assert status updated and saved
        assertEquals(OrderStatus.CANCELLED, oldOrder.getStatus());
        verify(orderRepository).save(oldOrder);

        // Assert rollback triggered with expected items
        verify(inventoryClient).rollbackStock(argThat(req ->
                req.getProducts().stream().anyMatch(p -> p.getProductId().equals(1L)) &&
                        req.getProducts().stream().anyMatch(p -> p.getProductId().equals(2L))
        ));

        // Assert audit log written
        verify(auditLogRepository).save(any(OrderAuditLog.class));
    }

    @Test
    void shouldSkipIfNoExpiredOrdersFound() {
        when(orderRepository.findExpiredUnshippedOrders(any())).thenReturn(List.of());

        scheduler.expireOldOrders();

        verify(orderRepository, never()).save(any());
        verify(inventoryClient, never()).rollbackStock(any());
        verify(auditLogRepository, never()).save(any());
    }
}
