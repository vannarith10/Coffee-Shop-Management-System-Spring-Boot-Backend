package com.coffeeshop.api.service;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.dto.order.CashOrderResponse;
import com.coffeeshop.api.dto.order.CreateOrderRequest;
import com.coffeeshop.api.dto.order.OrderMessageToBarista;
import com.coffeeshop.api.dto.order.UpdateOrderStatusResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    CashOrderResponse createOrder (CreateOrderRequest request);

    Order confirmAndSendToBarista (UUID orderId);

    UpdateOrderStatusResponse updateOrderStatus (UUID orderId, String status);

    List<OrderMessageToBarista> findRecentVisibleOrders ();

}
