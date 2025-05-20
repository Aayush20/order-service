package org.example.orderservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.orderservice.clients.InventoryClient;
import org.example.orderservice.clients.ProductClient;
import org.example.orderservice.dtos.RollbackStockRequestDto;
import org.example.orderservice.models.InventoryRollbackTask;
import org.example.orderservice.models.Order;
import org.example.orderservice.models.OrderItem;
import org.example.orderservice.models.OrderStatus;
import org.example.orderservice.repositories.InventoryRollbackTaskRepository;
import org.example.orderservice.repositories.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryRollbackService {

    private static final Logger log = LoggerFactory.getLogger(InventoryRollbackService.class);

    private final InventoryClient inventoryClient;

    private final OrderRepository orderRepository;
    private final InventoryRollbackTaskRepository rollbackTaskRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InventoryRollbackService(InventoryClient inventoryClient,
                                    OrderRepository orderRepository,
                                    InventoryRollbackTaskRepository rollbackTaskRepository) {
        this.inventoryClient = inventoryClient;
        this.orderRepository = orderRepository;
        this.rollbackTaskRepository = rollbackTaskRepository;
    }

    public void rollbackInventory(Order order, String reason) {
        List<RollbackStockRequestDto.ProductRollbackEntry> entries = order.getOrderItems().stream()
                .map(item -> new RollbackStockRequestDto.ProductRollbackEntry(item.getProductId(), item.getQuantity()))
                .toList();
        RollbackStockRequestDto rollbackRequest = new RollbackStockRequestDto(entries, reason);


        try {
            inventoryClient.rollbackStock(rollbackRequest);
            log.info("✅ Successfully rolled back stock for order {}", order.getId());
        } catch (Exception ex) {
            log.error("❌ Failed to rollback stock for order {}. Queuing for retry. Reason: {}", order.getId(), ex.getMessage());

            try {
                InventoryRollbackTask task = new InventoryRollbackTask();
                task.setOrderId(order.getId());
                task.setPayload(objectMapper.writeValueAsString(rollbackRequest));
                task.setRetryCount(0);
                task.setLastTriedAt(Instant.now());
                rollbackTaskRepository.save(task);
            } catch (Exception e) {
                log.error("⚠️ Could not save rollback retry task: {}", e.getMessage());
            }
        }
    }

    public void rollbackInventoryForFailedPayment(String orderIdStr, String userId, String reason) {
        Order order = orderRepository.findById(Long.parseLong(orderIdStr))
                .orElseThrow(() -> new RuntimeException("Order not found for ID: " + orderIdStr));

        if (order.getStatus() != OrderStatus.PLACED) {
            log.warn("Skipping rollback. Order {} is not in PLACED state.", orderIdStr);
            return;
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        rollbackInventory(order, reason);
    }

    public void retryFailedTasks() {
        List<InventoryRollbackTask> failedTasks = rollbackTaskRepository.findAll(); // You can filter on retry count or time
        for (InventoryRollbackTask task : failedTasks) {
            try {
                RollbackStockRequestDto dto = objectMapper.readValue(task.getPayload(), RollbackStockRequestDto.class);
                inventoryClient.rollbackStock(dto);

                rollbackTaskRepository.delete(task); // Success: clean up
                log.info("✅ Retried rollback for order {}", task.getOrderId());
            } catch (Exception ex) {
                task.setRetryCount(task.getRetryCount() + 1);
                task.setLastTriedAt(Instant.now());
                rollbackTaskRepository.save(task);
                log.warn("❌ Retry failed for rollback order {}: {}", task.getOrderId(), ex.getMessage());
            }
        }
    }

}

