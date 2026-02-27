package com.coffeeshop.api.controller;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.dto.order.OrderMessage;
import com.coffeeshop.api.mapper.OrderMapper;
import com.coffeeshop.api.service.BaristaOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/barista-order")
@RequiredArgsConstructor
public class BaristaOrderController {

    private final BaristaOrderService baristaOrderService;


    // Start PREPARING
    @PostMapping("/orders/{orderId}/start")
    public OrderMessage preparing (@RequestParam UUID orderId) {
        Order updated = baristaOrderService.startPreparing(orderId);
        return OrderMapper.toOrderMessage(updated);
    }


    // Mark DONE
    @PostMapping("/orders/{orderId}/done")
    public OrderMessage done (@RequestParam UUID orderId) {
        Order updated = baristaOrderService.markDone(orderId);
        return OrderMapper.toOrderMessage(updated);
    }

}
