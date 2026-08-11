package com.coffeeshop.api.controller;

import com.coffeeshop.api.dto.order.CashOrderResponse;
import com.coffeeshop.api.dto.order.CreateOrderRequest;
import com.coffeeshop.api.dto.order.GetCreatedOrderResponse;
import com.coffeeshop.api.service.OrderCreationService;
import com.coffeeshop.api.service.OrderStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/cashier-order")
@PreAuthorize("hasRole('CASHIER')")
public class CashierOrderController {

    private final OrderCreationService orderCreationService;
    private final OrderStatusService orderStatusService;



    // CREATE CASH ORDER
    @PostMapping("/create-cash-order")
    public ResponseEntity<CashOrderResponse> createCashOrder (
            @Valid @RequestBody CreateOrderRequest request
            ) {
        return ResponseEntity.ok(orderCreationService.createCashOrder(request));
    }



    /**
     * Retrieves a summary of a newly created order.
     *
     * This endpoint is called by the cashier confirmation screen immediately
     * after order creation. It returns key information required to verify
     * the order before proceeding with payment or fulfillment.
     *
     * Access is restricted to users with the CASHIER role.
     *
     * @param orderId Unique identifier of the created order.
     * @return Order summary including payment method, status, total price,
     * item count, and unit count.
     */
    @GetMapping("/created/{orderId}")
    @PreAuthorize("hasRole('CASHIER')")
    public ResponseEntity<GetCreatedOrderResponse> getCreatedOrder (
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok().body(orderStatusService.getCreatedOrder(orderId));
    }



    // CONFIRM ORDER
    @PostMapping("/{id}/confirm")
    public ResponseEntity<ConfirmResponse> confirmOrder (
            @PathVariable UUID id
            ) {
        var order = orderStatusService.confirmAndSendToBarista(id);
        return ResponseEntity.ok(new ConfirmResponse(
                "success",
                "Order sent to barista",
                order.getId(),
                order.getStatus().name()));
    }


    public record ConfirmResponse(String status, String message, UUID orderId, String orderStatus) {}

}
