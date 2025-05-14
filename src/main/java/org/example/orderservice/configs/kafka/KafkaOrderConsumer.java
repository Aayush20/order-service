package org.example.orderservice.configs.kafka;

import org.example.orderservice.dtos.RollbackRequestDTO;
import org.example.orderservice.models.*;
import org.example.orderservice.repositories.InventoryRollbackTaskRepository;
import org.example.orderservice.repositories.OrderAuditLogRepository;
import org.example.orderservice.repositories.OrderRepository;
import org.example.orderservice.services.InventoryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class KafkaOrderConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaOrderConsumer.class);
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final OrderAuditLogRepository auditLogRepository;
    private final InventoryRollbackTaskRepository rollbackTaskRepository;


    public KafkaOrderConsumer(OrderRepository orderRepository,
                              InventoryClient inventoryClient,
                              OrderAuditLogRepository auditLogRepository,
                              InventoryRollbackTaskRepository rollbackTaskRepository) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.auditLogRepository = auditLogRepository;
        this.rollbackTaskRepository = rollbackTaskRepository;
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

    @KafkaListener(topics = "payment_failed", groupId = "order-service")
    @Transactional
    public void handlePaymentFailedEvent(String orderIdStr) {
        Order order = null;
        List<Long> productIds = null;
        try {
            Long orderId = Long.parseLong(orderIdStr);
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found for ID: " + orderId));

            if (order.getStatus() == OrderStatus.PLACED) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);

                productIds = order.getOrderItems().stream()
                        .map(OrderItem::getProductId)
                        .toList();

                inventoryClient.rollbackStock(new RollbackRequestDTO(productIds));
                auditLogRepository.save(new OrderAuditLog(orderId, "system", "CANCELLED_DUE_TO_PAYMENT_FAILED"));
                logger.info("❌ Order {} cancelled due to payment failure. Inventory rollback triggered.", orderId);
            } else {
                logger.info("Skipping payment failure rollback. Order {} is not in PLACED state.", orderId);
            }
        } catch (Exception ex) {
            if (order != null && productIds != null) {
                InventoryRollbackTask task = new InventoryRollbackTask();
                task.setOrderId(order.getId());
                task.setProductIds(productIds);
                rollbackTaskRepository.save(task);
            }
            logger.error("❗ Error processing payment failed event: {}", ex.getMessage(), ex);
        }
    }


}
