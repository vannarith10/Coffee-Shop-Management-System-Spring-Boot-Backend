package com.coffeeshop.api.service;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.dto.order.UpdateOrderStatusResponse;

import java.util.UUID;

public interface OrderStatusService {

    Order confirmAndSendToBarista (UUID orderId);

    UpdateOrderStatusResponse updateOrderStatus (UUID orderId, String status);

}
