package org.example.orderservice.repositories;

import org.example.orderservice.models.RetryDeadLetterLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetryDeadLetterLogRepository extends JpaRepository<RetryDeadLetterLog, Long> {
}
