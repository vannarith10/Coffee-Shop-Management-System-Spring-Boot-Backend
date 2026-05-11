package com.coffeeshop.api.repository;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.domain.enums.OrderStatus;
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



    // Barista Performance Metrics
    @Query("""
        select o from Order o
            where o.status = com.coffeeshop.api.domain.enums.OrderStatus.DONE
                and o.doneAt >= :from and o.doneAt < :to
                    and o.preparationStartedAt is not null
    """)
    List<Order> findCompletedToday (Instant from, Instant to);




    // Total Revenue in a period (for Summary - Net Revenue)
    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.status = 'DONE'
            AND o.createdAt >= :start
            AND o.createdAt < :end
    """)
    BigDecimal getTotalRevenue (@Param("start") Instant start, @Param("end") Instant end);




    // Gross Profit in a period
    @Query("""
        SELECT COALESCE(SUM(o.totalAmount - (o.subtotalAmount * 0.40)), 0)
        FROM Order o
        WHERE o.status = 'DONE'
            AND o.createdAt >= :start
            AND o.createdAt < :end
    """)
    BigDecimal getGrossProfit(@Param("start") Instant start, @Param("end") Instant end);




    // Daily Revenue for Revenue Trends (per day)
    @Query("""
        SELECT function('date', o.createdAt), COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.status = 'DONE'
          AND o.createdAt >= :start
          AND o.createdAt < :end
        GROUP BY function('date', o.createdAt)
        ORDER BY function('date', o.createdAt)
    """)
    List<Object[]> getDailyRevenue(
            @Param("start") Instant start,
            @Param("end") Instant end
    );



    // Sales by Category
    @Query("""
        SELECT p.category, COALESCE(SUM(oi.totalPrice), 0)
        FROM Order o
        JOIN o.items oi
        JOIN oi.product p
        WHERE o.status = 'DONE'
            AND o.createdAt >= :start
            AND o.createdAt < :end
        GROUP BY p.category
        ORDER BY COALESCE(SUM(oi.totalPrice), 0) DESC
    """)
    List<Object[]> getSalesByCategory(@Param("start") Instant start, @Param("end") Instant end);





    // Hourly Order Count per Weekday
    @Query(value = """
    SELECT
        EXTRACT(DOW FROM o.created_at) AS dow,
        EXTRACT(HOUR FROM o.created_at) AS hour,
        COUNT(o.id) AS total
    FROM orders o
    WHERE o.status = 'DONE'
      AND o.created_at >= :start
      AND o.created_at < :end
    GROUP BY
        EXTRACT(DOW FROM o.created_at),
        EXTRACT(HOUR FROM o.created_at)
    ORDER BY
        dow, hour
    """, nativeQuery = true)
    List<Object[]> getHourlyDistribution(
            @Param("start") Instant start,
            @Param("end") Instant end
    );






}






















