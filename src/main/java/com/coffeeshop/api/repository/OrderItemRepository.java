package com.coffeeshop.api.repository;

import com.coffeeshop.api.domain.OrderItem;
import com.coffeeshop.api.domain.enums.OrderStatus;
//import com.coffeeshop.api.dto.product.TopSellingProductRow;
import com.coffeeshop.api.dto.adminDashboard.TopSellingProductProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {


    @Query(
            value = """
        select new com.coffeeshop.api.dto.adminDashboard.TopSellingProductProjection(
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
    """,
            countQuery = """
        select count(distinct p.id)
        from OrderItem oi
        join oi.order o
        join oi.product p
        where o.status = :doneStatus
          and o.doneAt >= :start
          and o.doneAt < :end
    """
    )
    Page<TopSellingProductProjection> findTopSellingProductsByDateRange(
            @Param("doneStatus") OrderStatus doneStatus,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable
    );


}
