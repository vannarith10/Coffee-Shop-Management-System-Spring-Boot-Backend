package com.coffeeshop.api.service;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.dto.order.CashOrderResponse;
import com.coffeeshop.api.dto.order.CreateOrderRequest;

import java.util.UUID;

public interface OrderService {

    CashOrderResponse createOrder (CreateOrderRequest request);

    Order confirmAndSendToBarista (UUID orderId);

}
