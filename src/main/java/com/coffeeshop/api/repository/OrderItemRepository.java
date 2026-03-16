package com.coffeeshop.api.repository;

import com.coffeeshop.api.domain.OrderItem;
import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.dto.product.TopSellingProductRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @Query("""
        select new com.coffeeshop.api.dto.product.TopSellingProductRow(
            p.id,
            p.name,
            p.imageKey,
            sum(oi.quantity)
            )
                from OrderItem oi
                join oi.order o
                join oi.product p
                where o.status = :doneStatus
                and o.doneAt >= :start
                and o.doneAt < :end
                group by p.id, p.name, p.imageKey
                order by sum(oi.quantity) desc
    """)
    List<TopSellingProductRow> findTopSellingProductsForMonth (
            @Param("doneStatus") OrderStatus doneStatus,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

}
