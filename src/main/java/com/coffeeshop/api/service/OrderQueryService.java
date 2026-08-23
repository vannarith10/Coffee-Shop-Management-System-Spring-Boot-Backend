package com.coffeeshop.api.service;

import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.dto.order.BaristaOrderItem;
import com.coffeeshop.api.dto.order.BaristaOrderQueue;

import java.util.List;

public interface OrderQueryService {


    BaristaOrderQueue getOrders (int page,
                                 int size,
                                 OrderStatus status);

}
