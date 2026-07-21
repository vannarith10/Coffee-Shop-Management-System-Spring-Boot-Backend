package com.coffeeshop.api.repository;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.dto.adminDashboard.report.DailyRevenueProjection;
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




    // ======================================================
    // Total Revenue in a period (for Summary - Net Revenue)
    // ====================================================== modified
    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.status = 'DONE'
            AND o.doneAt >= :start
            AND o.doneAt < :end
    """)
    BigDecimal getTotalRevenue (@Param("start") Instant start, @Param("end") Instant end);




    // =========================
    // Gross Profit in a period
    // ========================= modified
    @Query("""
        SELECT COALESCE(SUM(o.totalAmount - (o.subtotalAmount * 0.40)), 0)
        FROM Order o
        WHERE o.status = 'DONE'
            AND o.doneAt >= :start
            AND o.doneAt < :end
    """)
    BigDecimal getGrossProfit(@Param("start") Instant start, @Param("end") Instant end);




    // ===========================================
    // Daily Revenue for Revenue Trends (per day)
    // =========================================== modified
    @Query("""
        SELECT function('date', o.doneAt), COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.status = 'DONE'
          AND o.doneAt >= :start
          AND o.doneAt < :end
        GROUP BY function('date', o.doneAt)
        ORDER BY function('date', o.doneAt)
    """)
    List<Object[]> getDailyRevenue(
            @Param("start") Instant start,
            @Param("end") Instant end
    );



    // ===================
    // Sales by Category
    // =================== modified
    @Query("""
        SELECT p.category, COALESCE(SUM(oi.totalPrice), 0)
        FROM Order o
        JOIN o.items oi
        JOIN oi.product p
        WHERE o.status = 'DONE'
            AND o.doneAt >= :start
            AND o.doneAt < :end
        GROUP BY p.category
        ORDER BY COALESCE(SUM(oi.totalPrice), 0) DESC
    """)
    List<Object[]> getSalesByCategory(@Param("start") Instant start, @Param("end") Instant end);





    // ================================
    // Hourly Order Count per Weekday
    // ================================
    @Query(value = """
        SELECT
            EXTRACT(DOW FROM (o.done_at AT TIME ZONE 'Asia/Phnom_Penh')) AS dow,
            EXTRACT(HOUR FROM (o.done_at AT TIME ZONE 'Asia/Phnom_Penh')) AS hour,
            COUNT(o.id) AS total
        FROM orders o
        WHERE o.status = 'DONE'
          AND o.done_at >= :start
          AND o.done_at < :end
        GROUP BY
            EXTRACT(DOW FROM (o.done_at AT TIME ZONE 'Asia/Phnom_Penh')),
            EXTRACT(HOUR FROM (o.done_at AT TIME ZONE 'Asia/Phnom_Penh'))
        ORDER BY
            dow, hour
    """, nativeQuery = true)
    List<Object[]> getHourlyDistribution(
            @Param("start") Instant start,
            @Param("end") Instant end
    );


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
}

