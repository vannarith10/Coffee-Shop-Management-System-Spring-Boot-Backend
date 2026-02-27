package com.coffeeshop.api.controller;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderConfirmController {

    private final OrderService orderService;

    // Cashier clicks button Confirm -> call this
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<ConfirmResponse> confirm (@PathVariable UUID orderId) {
        Order order = orderService.confirmAndSendToBarista(orderId);

        return ResponseEntity.ok(new ConfirmResponse(
                "success",
                "Order sent to barista.",
                order.getId(),
                order.getStatus().name()
        ));
    }


    // DTO
    public record ConfirmResponse(
            String status,
            String message,
            UUID orderId,
            String orderStatus
    ) {}


}
