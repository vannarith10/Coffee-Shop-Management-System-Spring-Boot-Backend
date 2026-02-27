package com.coffeeshop.api.serviceimpl;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.dto.order.OrderMessage;
import com.coffeeshop.api.mapper.OrderMapper;
import com.coffeeshop.api.repository.OrderRepository;
import com.coffeeshop.api.service.BaristaOrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BaristaOrderQueryServiceImpl implements BaristaOrderQueryService {

    private final OrderRepository orderRepository;

    @Override
    public List<OrderMessage> findRecentVisibleOrders() {

        EnumSet<OrderStatus> visible = EnumSet.of(
                OrderStatus.QUEUED,
                OrderStatus.PREPARING,
                OrderStatus.DONE);
        List<Order> orders = orderRepository.findTop50ByStatusInOrderByCreatedAtAsc(visible);

        return orders.stream().map(OrderMapper::toOrderMessage).toList();
    }
}
