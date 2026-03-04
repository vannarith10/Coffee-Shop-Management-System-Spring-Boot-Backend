package com.coffeeshop.api.controller;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.dto.order.*;
import com.coffeeshop.api.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;


    @PostMapping("/create-order")
    public ResponseEntity<CashOrderResponse> createOrder (@RequestBody @Valid CreateOrderRequest request) {
        CashOrderResponse body = orderService.createOrder(request);
        return ResponseEntity.ok(body);
    }


    @PreAuthorize("hasRole('BARISTA')")
    @PutMapping("/{orderId}/update-status")
    public ResponseEntity<UpdateOrderStatusResponse> updateOrderStatus (@PathVariable UUID orderId,
                                                                        @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request.status()));
    }

    @GetMapping("/get-orders")
    public List<OrderMessageToBarista> list () {
        return orderService.findRecentVisibleOrders();
    }

}
