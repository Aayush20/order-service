package org.example.orderservice.schedulers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.example.orderservice.clients.InventoryClient;
import org.example.orderservice.dtos.RollbackStockRequestDto;
import org.example.orderservice.models.InventoryRollbackTask;
import org.example.orderservice.repositories.InventoryRollbackTaskRepository;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RollbackRetryScheduler(InventoryRollbackTaskRepository rollbackRepo, InventoryClient inventoryClient, MeterRegistry meterRegistry) {
        this.rollbackRepo = rollbackRepo;
        this.inventoryClient = inventoryClient;
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedRate = 30000)
    public void retryFailedRollbacks() {
        List<InventoryRollbackTask> tasks = rollbackRepo.findTop10BySuccessFalseOrderByRetryCountAscCreatedAtAsc();

        for (InventoryRollbackTask task : tasks) {
            try {
                RollbackStockRequestDto request = objectMapper.readValue(task.getPayload(), RollbackStockRequestDto.class);
                inventoryClient.rollbackStock(request);
                task.setSuccess(true);
                meterRegistry.counter("rollback.retry.success").increment();
                logger.info("✅ Retry succeeded for order {}", task.getOrderId());
            } catch (Exception ex) {
                task.setRetryCount(task.getRetryCount() + 1);
                meterRegistry.counter("rollback.retry.failure").increment();
                logger.warn("❌ Retry failed for order {} (attempt {})", task.getOrderId(), task.getRetryCount());
            }

            task.setLastTriedAt(Instant.now());
            rollbackRepo.save(task);
        }
    }
}
