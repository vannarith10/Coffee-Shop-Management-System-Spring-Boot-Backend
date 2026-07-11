package com.coffeeshop.api.service.impl;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.dto.order.OrderMessageToBarista;
import com.coffeeshop.api.mapper.OrderMapper;
import com.coffeeshop.api.repository.OrderRepository;
import com.coffeeshop.api.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl implements OrderQueryService {


    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private static final ZoneId BUSINESS_TZ = ZoneId.of("Asia/Phnom_Penh");


    @Override
    public List<OrderMessageToBarista> baristaGetsOrders() {
        Instant cutoff = ZonedDateTime.now(BUSINESS_TZ).toInstant().minus(7, ChronoUnit.DAYS);
        var statuses = EnumSet.of(OrderStatus.QUEUED, OrderStatus.PREPARING);

        List<Order> orders = orderRepository.findVisibleOrders(statuses, cutoff);

        return orders.stream()
                .map(orderMapper::toOrderMessageResponseDto)
                .toList();
    }

}
