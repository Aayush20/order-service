
package org.example.orderservice.schedulers;

import org.example.orderservice.models.*;
import org.example.orderservice.repositories.InventoryRollbackTaskRepository;
import org.example.orderservice.repositories.OrderAuditLogRepository;
import org.example.orderservice.repositories.OrderRepository;
import org.example.orderservice.services.InventoryClient;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderExpirySchedulerTest {

    private OrderRepository orderRepository;
    private InventoryClient inventoryClient;
    private OrderAuditLogRepository auditLogRepository;
    private InventoryRollbackTaskRepository rollbackTaskRepository;
    private OrderExpiryScheduler scheduler;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        inventoryClient = mock(InventoryClient.class);
        auditLogRepository = mock(OrderAuditLogRepository.class);
        rollbackTaskRepository = mock(InventoryRollbackTaskRepository.class);
        scheduler = new OrderExpiryScheduler(orderRepository, auditLogRepository, inventoryClient, new SimpleMeterRegistry());
    }

    @Test
    void testExpireOldPlacedOrders() {
        Order order1 = new Order();
        order1.setId(1L);
        order1.setStatus(OrderStatus.PLACED);
        order1.setOrderDate(Date.from(Instant.now().minusSeconds(1200)));
        order1.setUserId("user1");

        OrderItem item1 = new OrderItem(101L, 2);
        OrderItem item2 = new OrderItem(102L, 1);
        order1.setOrderItems(Arrays.asList(item1, item2));

        when(orderRepository.findExpiredUnshippedOrders(any())).thenReturn(List.of(order1));

        scheduler.expireOldOrders();

        verify(orderRepository).save(order1);
        assertEquals(OrderStatus.CANCELLED, order1.getStatus());
        verify(inventoryClient).rollbackStock(argThat(req -> req.getProductIds().contains(101L) && req.getProductIds().contains(102L)));
        verify(auditLogRepository).save(any());
    }
}
