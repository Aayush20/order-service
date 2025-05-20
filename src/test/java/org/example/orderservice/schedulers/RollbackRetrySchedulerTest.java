package org.example.orderservice.schedulers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.example.orderservice.clients.InventoryClient;
import org.example.orderservice.dtos.RollbackStockRequestDto;
import org.example.orderservice.models.InventoryRollbackTask;
import org.example.orderservice.repositories.InventoryRollbackTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;

class RollbackRetrySchedulerTest {

    @Mock private InventoryRollbackTaskRepository rollbackRepo;
    @Mock private InventoryClient inventoryClient;

    private RollbackRetryScheduler scheduler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scheduler = new RollbackRetryScheduler(
                rollbackRepo,
                inventoryClient,
                new SimpleMeterRegistry()
        );
    }

    @Test
    void shouldRetryRollbackTaskSuccessfully() throws Exception {
        RollbackStockRequestDto request = new RollbackStockRequestDto(
                List.of(new RollbackStockRequestDto.ProductRollbackEntry(1L, 2)),
                "retry test"
        );

        InventoryRollbackTask task = new InventoryRollbackTask();
        task.setOrderId(123L);
        task.setRetryCount(0);
        task.setSuccess(false);
        task.setPayload(objectMapper.writeValueAsString(request));

        when(rollbackRepo.findTop10BySuccessFalseOrderByRetryCountAscCreatedAtAsc())
                .thenReturn(List.of(task));

        scheduler.retryFailedRollbacks();

        verify(inventoryClient).rollbackStock(any());
        verify(rollbackRepo).save(argThat(t -> t.isSuccess()));
    }

    @Test
    void shouldHandleRollbackFailureAndIncrementRetryCount() throws Exception {
        RollbackStockRequestDto request = new RollbackStockRequestDto(
                List.of(new RollbackStockRequestDto.ProductRollbackEntry(2L, 5)),
                "fail test"
        );

        InventoryRollbackTask task = new InventoryRollbackTask();
        task.setOrderId(456L);
        task.setRetryCount(1);
        task.setSuccess(false);
        task.setPayload(objectMapper.writeValueAsString(request));

        when(rollbackRepo.findTop10BySuccessFalseOrderByRetryCountAscCreatedAtAsc())
                .thenReturn(List.of(task));

        doThrow(new RuntimeException("Simulated failure")).when(inventoryClient).rollbackStock(any());

        scheduler.retryFailedRollbacks();

        verify(rollbackRepo).save(argThat(t -> !t.isSuccess() && t.getRetryCount() == 2));
    }
}
