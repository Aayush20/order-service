package org.example.orderservice.schedulers;

import org.example.orderservice.dtos.RollbackRequestDTO;
import org.example.orderservice.models.Order;
import org.example.orderservice.models.OrderAuditLog;
import org.example.orderservice.models.OrderItem;
import org.example.orderservice.models.OrderStatus;
import org.example.orderservice.repositories.OrderAuditLogRepository;
import org.example.orderservice.repositories.OrderRepository;
import org.example.orderservice.clients.InventoryClient;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class OrderExpiryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OrderExpiryScheduler.class);

    private final OrderRepository orderRepository;
    private final OrderAuditLogRepository auditLogRepository;
    private final InventoryClient inventoryClient;
    private final MeterRegistry meterRegistry;

    @Value("${order.expiry.minutes}")
    private int expiryMinutes;

    public OrderExpiryScheduler(OrderRepository orderRepository,
                                OrderAuditLogRepository auditLogRepository,
                                InventoryClient inventoryClient,
                                MeterRegistry meterRegistry) {
        this.orderRepository = orderRepository;
        this.auditLogRepository = auditLogRepository;
        this.inventoryClient = inventoryClient;
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedRate = 60000) // every 1 minute
    public void expireOldOrders() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -expiryMinutes);
        Date cutoff = calendar.getTime();

        List<Order> expiredOrders = orderRepository.findExpiredUnshippedOrders(cutoff);

        for (Order order : expiredOrders) {
            logger.info("⏳ Expiring order {} placed at {}", order.getId(), order.getOrderDate());
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            // Trigger rollback
            List<Long> productIds = order.getOrderItems().stream()
                    .map(OrderItem::getProductId)
                    .toList();

            inventoryClient.rollbackStock(new RollbackRequestDTO(productIds));

            auditLogRepository.save(new OrderAuditLog(order.getId(), "system", "EXPIRED_AUTO_CANCELLED"));
            meterRegistry.counter("orders.expired.total").increment();
        }
    }
}
