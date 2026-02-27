package com.coffeeshop.api.service;

import com.coffeeshop.api.domain.Order;

import java.util.UUID;

public interface BaristaOrderService {

    Order startPreparing (UUID orderId);

    Order markDone (UUID orderId);

}
