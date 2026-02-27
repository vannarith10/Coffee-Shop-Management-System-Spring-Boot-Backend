package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.order.OrderMessage;

import java.util.List;

public interface BaristaOrderQueryService {

    List<OrderMessage> findRecentVisibleOrders ();

}
