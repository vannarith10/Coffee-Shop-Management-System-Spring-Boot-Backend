package com.coffeeshop.api.repository;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.domain.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findTopByCreatedAtBetweenOrderByCreatedAtDesc(
            Instant start,
            Instant end
    );

    List<Order> findTop50ByStatusInOrderByCreatedAtAsc(Collection<OrderStatus> statuses);

    List<Order> findByStatusInOrderByCreatedAtAsc(Collection<OrderStatus> statuses, Pageable pageable);


    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.createdAt >= :start AND o.createdAt < :end
          AND o.status = :doneStatus
          AND o.cancelledAt IS NULL
    """)
    BigDecimal sumRevenueBetween(
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("doneStatus") OrderStatus doneStatus
    );


    @Query("""
        SELECT COUNT(o)
        FROM Order o
        WHERE o.createdAt >= :start AND o.createdAt < :end
          AND o.status = :doneStatus
          AND o.cancelledAt IS NULL
    """)
    long countOrdersBetween(
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("doneStatus") OrderStatus doneStatus
    );



    // Get all active orders + recent done orders
    @Query("""
    SELECT o FROM Order o 
    WHERE o.status IN :activeStatuses 
    OR (o.status = 'DONE' AND o.createdAt > :cutoffTime)
    ORDER BY o.createdAt ASC
    """)
    List<Order> findVisibleOrders(
            @Param("activeStatuses") Collection<OrderStatus> activeStatuses,
            @Param("cutoffTime") Instant cutoffTime
    );




}
