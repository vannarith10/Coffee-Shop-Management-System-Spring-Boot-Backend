//package com.coffeeshop.api.service.oldService;
//
//import com.coffeeshop.api.domain.Order;
//import com.coffeeshop.api.dto.order.*;
//
//import java.time.Duration;
//import java.util.List;
//import java.util.UUID;
//
//public interface OrderService {
//
//    CashOrderResponse createOrder (CreateOrderRequest request);
//
//    Order confirmAndSendToBarista (UUID orderId);
//
//    UpdateOrderStatusResponse updateOrderStatus (UUID orderId, String status);
//
//    List<OrderMessageToBarista> findRecentVisibleOrders ();
//
//    PerformanceMetricsResponse performanceMetrics (Duration slaTarget);
//
//}
