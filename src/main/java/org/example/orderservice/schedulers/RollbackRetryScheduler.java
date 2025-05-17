package org.example.orderservice.schedulers;

import io.micrometer.core.instrument.MeterRegistry;
import org.example.orderservice.dtos.RollbackRequestDTO;
import org.example.orderservice.models.InventoryRollbackTask;
import org.example.orderservice.repositories.InventoryRollbackTaskRepository;
import org.example.orderservice.clients.InventoryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class RollbackRetryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RollbackRetryScheduler.class);

    private final InventoryRollbackTaskRepository rollbackRepo;
    private final InventoryClient inventoryClient;
    private final MeterRegistry meterRegistry;

    public RollbackRetryScheduler(InventoryRollbackTaskRepository rollbackRepo, InventoryClient inventoryClient, MeterRegistry meterRegistry) {
        this.rollbackRepo = rollbackRepo;
        this.inventoryClient = inventoryClient;
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedRate = 30000) // run every 30 seconds
    public void retryFailedRollbacks() {
        List<InventoryRollbackTask> tasks = rollbackRepo.findTop10BySuccessFalseOrderByRetryCountAscCreatedAtAsc();

        for (InventoryRollbackTask task : tasks) {
            try {
                inventoryClient.rollbackStock(new RollbackRequestDTO(task.getProductIds()));
                task.setSuccess(true);
                meterRegistry.counter("rollback.retry.success").increment();
                logger.info("✅ Retry succeeded for order {}", task.getOrderId());
            } catch (Exception ex) {
                task.setRetryCount(task.getRetryCount() + 1);
                meterRegistry.counter("rollback.retry.failure").increment();
                logger.warn("❗ Retry failed for order {} (attempt {})", task.getOrderId(), task.getRetryCount());
            }
            task.setLastTriedAt(Instant.now());
            rollbackRepo.save(task);
        }
    }
}
