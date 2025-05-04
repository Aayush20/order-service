package org.example.orderservice.repositories;

import org.example.orderservice.models.OrderAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderAuditLogRepository extends JpaRepository<OrderAuditLog, Long> {
    List<OrderAuditLog> findByOrderIdOrderByCreatedAtDesc(Long orderId);

}
