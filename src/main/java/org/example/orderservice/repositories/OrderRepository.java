package org.example.orderservice.repositories;

import org.example.orderservice.models.Order;
import org.example.orderservice.models.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(String userId);
    Page<Order> findByUserId(String userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = 'PLACED' AND o.orderDate <= :cutoff")
    List<Order> findExpiredUnshippedOrders(@Param("cutoff") Date cutoff);

    List<Order> findByStatusAndOrderDateBefore(OrderStatus status, Date cutoffDate);





}
