package org.example.orderservice.repositories;

import org.example.orderservice.models.InventoryRollbackTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRollbackTaskRepository extends JpaRepository<InventoryRollbackTask, Long> {
    List<InventoryRollbackTask> findTop10BySuccessFalseOrderByRetryCountAscCreatedAtAsc();
}
