package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.order.CashOrderResponse;
import com.coffeeshop.api.dto.order.CreateOrderRequest;

public interface OrderCreationService {

    CashOrderResponse createCashOrder (CreateOrderRequest request);

}
