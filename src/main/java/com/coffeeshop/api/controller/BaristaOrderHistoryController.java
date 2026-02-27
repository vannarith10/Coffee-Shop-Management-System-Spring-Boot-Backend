package com.coffeeshop.api.controller;


import com.coffeeshop.api.dto.order.OrderMessage;
import com.coffeeshop.api.service.BaristaOrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/barista")
@RequiredArgsConstructor
public class BaristaOrderHistoryController {

    private final BaristaOrderQueryService baristaOrderQueryService;

    @GetMapping("/orders")
    public List<OrderMessage> list () {
        return baristaOrderQueryService.findRecentVisibleOrders();
    }

}
