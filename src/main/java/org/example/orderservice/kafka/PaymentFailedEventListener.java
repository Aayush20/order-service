package org.example.orderservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.orderservice.models.InventoryRollbackTask;
import org.example.orderservice.models.Order;
import org.example.orderservice.models.OrderStatus;
import org.example.orderservice.repositories.InventoryRollbackTaskRepository;
import org.example.orderservice.repositories.OrderRepository;
import org.example.orderservice.services.InventoryRollbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
public class PaymentFailedEventListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentFailedEventListener.class);

    private final InventoryRollbackService rollbackService;
    private final OrderRepository orderRepository;
    private final InventoryRollbackTaskRepository rollbackTaskRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentFailedEventListener(InventoryRollbackService rollbackService,
                                      OrderRepository orderRepository,
                                      InventoryRollbackTaskRepository rollbackTaskRepository) {
        this.rollbackService = rollbackService;
        this.orderRepository = orderRepository;
        this.rollbackTaskRepository = rollbackTaskRepository;
    }

    @KafkaListener(topics = "payment.failed", groupId = "order-service-group")
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        logger.warn("📩 Received payment.failed event for orderId={} reason={}", event.getOrderId(), event.getFailureReason());

        Long orderId = null;
        try {
            orderId = Long.parseLong(event.getOrderId());
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + event.getOrderId()));

            if (order.getStatus() != OrderStatus.PLACED) {
                logger.warn("Order {} not in PLACED state. Skipping rollback.", orderId);
                return;
            }

            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            rollbackService.rollbackInventory(order, event.getFailureReason());

        } catch (NumberFormatException e) {
            logger.error("❌ Invalid orderId format: {}", event.getOrderId());

            try {
                InventoryRollbackTask task = new InventoryRollbackTask();
                task.setOrderId(-1L); // use a default/fallback invalid ID
                task.setPayload(objectMapper.writeValueAsString(event));
                task.setRetryCount(0);
                task.setLastTriedAt(Instant.now());
                rollbackTaskRepository.save(task);
            } catch (Exception ex) {
                logger.error("⚠️ Failed to save rollback task for invalid ID: {}", ex.getMessage());
            }

        } catch (Exception e) {
            logger.error("❌ Error handling payment.failed event: {}", e.getMessage(), e);

            try {
                InventoryRollbackTask task = new InventoryRollbackTask();
                task.setOrderId(orderId != null ? orderId : -1L);
                task.setPayload(objectMapper.writeValueAsString(event));
                task.setRetryCount(0);
                task.setLastTriedAt(Instant.now());
                rollbackTaskRepository.save(task);
            } catch (Exception ex) {
                logger.error("⚠️ Failed to save rollback task: {}", ex.getMessage());
            }
        }
    }
}
