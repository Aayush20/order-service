//
//package org.example.orderservice.schedulers;
//
//import org.example.orderservice.dtos.RollbackRequestDTO;
//import org.example.orderservice.models.InventoryRollbackTask;
//import org.example.orderservice.repositories.InventoryRollbackTaskRepository;
//import org.example.orderservice.clients.InventoryClient;
//import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.mockito.Mockito.*;
//
//public class RollbackRetrySchedulerTest {
//
//    private InventoryRollbackTaskRepository rollbackRepo;
//    private InventoryClient inventoryClient;
//    private RollbackRetryScheduler scheduler;
//
//    @BeforeEach
//    void setup() {
//        rollbackRepo = mock(InventoryRollbackTaskRepository.class);
//        inventoryClient = mock(InventoryClient.class);
//        scheduler = new RollbackRetryScheduler(rollbackRepo, inventoryClient, new SimpleMeterRegistry());
//    }
//
//    @Test
//    void testRetryFailedRollbacks_success() {
//        InventoryRollbackTask task = new InventoryRollbackTask();
//        task.setOrderId(1L);
//        task.setProductIds(List.of(101L, 102L));
//        task.setRetryCount(2);
//        task.setSuccess(false);
//
//        when(rollbackRepo.findTop10BySuccessFalseOrderByRetryCountAscCreatedAtAsc()).thenReturn(List.of(task));
//
//        scheduler.retryFailedRollbacks();
//
//        verify(inventoryClient).rollbackStock(any(RollbackRequestDTO.class));
//        verify(rollbackRepo).save(any(InventoryRollbackTask.class));
//    }
//
//    @Test
//    void testRetryFailedRollbacks_failure() {
//        InventoryRollbackTask task = new InventoryRollbackTask();
//        task.setOrderId(2L);
//        task.setProductIds(List.of(201L));
//        task.setRetryCount(1);
//        task.setSuccess(false);
//
//        when(rollbackRepo.findTop10BySuccessFalseOrderByRetryCountAscCreatedAtAsc()).thenReturn(List.of(task));
//        doThrow(new RuntimeException("Simulated failure")).when(inventoryClient).rollbackStock(any());
//
//        scheduler.retryFailedRollbacks();
//
//        verify(rollbackRepo).save(any(InventoryRollbackTask.class));
//    }
//}
