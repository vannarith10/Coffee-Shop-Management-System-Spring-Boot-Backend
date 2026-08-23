package com.coffeeshop.api.controller;


import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.dto.order.*;
import com.coffeeshop.api.service.OrderQueryService;
import com.coffeeshop.api.service.OrderStatusService;
import com.coffeeshop.api.service.PerformanceMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/barista-order")
@PreAuthorize("hasRole('BARISTA')")
public class BaristaOrderController {

    private final OrderStatusService orderStatusService;
    private final OrderQueryService orderQueryService;
    private final PerformanceMetricsService performanceMetricsService;


    // UPDATE ORDER STATUS
    @PutMapping("/{id}/update-status")
    public ResponseEntity<UpdateOrderStatusResponse> updateStatus (
            @PathVariable UUID id,
            @RequestBody UpdateOrderStatusRequest request
            ) {
        return ResponseEntity.ok(orderStatusService.updateOrderStatus(id, request.status()));
    }



    @GetMapping("/retrieve")
    public ResponseEntity<BaristaOrderQueue> retrieveOrders (
            @RequestParam (required = false, defaultValue = "1") int page,
            @RequestParam (required = false, defaultValue = "10") int size,
            @RequestParam (required = false) OrderStatus status
            ) {
        return ResponseEntity.ok(orderQueryService.getOrders(
                page,
                size,
                status
        ));
    }




    // PERFORMANCE METRICS
    @GetMapping(value = "/today", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PerformanceMetricsResponse> metrics (
            @RequestParam(defaultValue = "PT4M") String sla
    ) {
        Duration target = Duration.parse(sla);
        if (target.isNegative() || target.isZero()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(performanceMetricsService.calculate(target));
    }


}
