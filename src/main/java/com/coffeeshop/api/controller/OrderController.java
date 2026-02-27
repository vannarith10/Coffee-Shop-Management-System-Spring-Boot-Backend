package com.coffeeshop.api.controller;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.dto.order.CreateOrderRequest;
import com.coffeeshop.api.dto.order.CashOrderResponse;
import com.coffeeshop.api.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
