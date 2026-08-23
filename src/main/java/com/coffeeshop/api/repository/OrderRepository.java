package com.coffeeshop.api.repository;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.dto.adminDashboard.report.DailyRevenueProjection;
import org.springframework.data.domain.Page;
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



    // ==========================
    // Sum revenue START to END
    // ========================== modified
    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.doneAt >= :start AND o.doneAt < :end
          AND o.status = :doneStatus
          AND o.cancelledAt IS NULL
    """)
    BigDecimal sumRevenueBetween(
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("doneStatus") OrderStatus doneStatus
    );


    // =================================
    // Count order between START to END
    // ================================= modified
    @Query("""
        SELECT COUNT(o)
        FROM Order o
        WHERE o.doneAt >= :start AND o.doneAt < :end
          AND o.status = :doneStatus
          AND o.cancelledAt IS NULL
    """)
    long countOrdersBetween(
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("doneStatus") OrderStatus doneStatus
    );



    // ===========================================
    // Get all active orders + recent done orders
    // ===========================================
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



    // =============================
    // Barista Performance Metrics
    // ============================= modified
    @Query("""
        select o from Order o
        where o.status = 'DONE'
        and o.doneAt >= :from and o.doneAt < :to
        and o.preparationStartedAt is not null
    """)
    List<Order> findCompletedToday (Instant from, Instant to);






    // ==========================================
    // Revenue Trends
    // ==========================================
    @Query("""
        SELECT DAY(o.createdAt) as day, COALESCE(SUM(o.totalAmount), 0) as revenue
        FROM Order o
        WHERE o.createdAt >= :start AND o.createdAt < :end
        AND o.status = 'DONE'
        GROUP BY DAY(o.createdAt)
        ORDER BY DAY(o.createdAt)
    """)
    List<DailyRevenueProjection> findDailyRevenue (Instant start, Instant end);





    /**
     * Retrieves orders for the KDS in the required processing order.
     *
     * Orders are filtered to include only active KDS statuses:
     * QUEUED, PREPARING, and DONE.
     *
     * The custom status priority ensures orders are displayed in this order:
     * 1. QUEUED     - waiting to be prepared
     * 2. PREPARING  - currently being prepared
     * 3. DONE       - preparation completed
     *
     * Within the same status, older orders are displayed first (FIFO)
     * based on their creation time.
     */
    @Query("""
        SELECT o FROM Order o
        WHERE o.status IN ('QUEUED', 'PREPARING', 'DONE')
        ORDER BY
            CASE o.status
                WHEN 'QUEUED' THEN 1
                WHEN 'PREPARING' THEN 2
                WHEN 'DONE' THEN 3
            END,
            o.createdAt ASC
    """)
    Page<Order> findAllByStatusPriority (Pageable pageable);




    /**
     * Retrieves orders with the specified status, sorted by creation time
     * in ascending order to process last order first (LIFO).
     */
    @Query("""
        SELECT o FROM Order o
        WHERE o.status = :status
            AND o.status IN ('QUEUED', 'PREPARING', 'DONE')
        ORDER BY o.createdAt DESC
    """)
    Page<Order> findByStatus (Pageable pageable, OrderStatus status);
}

