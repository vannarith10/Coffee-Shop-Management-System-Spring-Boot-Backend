package com.coffeeshop.api.service.impl;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.domain.OrderItem;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.dto.adminDashboard.BusinessAnalyticsSummaryResponse;
import com.coffeeshop.api.dto.order.GetCreatedOrderResponse;
import com.coffeeshop.api.dto.order.OrderEvent;
import com.coffeeshop.api.dto.order.PerformanceMetricsResponse;
import com.coffeeshop.api.dto.order.UpdateOrderStatusResponse;
import com.coffeeshop.api.mapper.OrderMapper;
import com.coffeeshop.api.repository.OrderRepository;
import com.coffeeshop.api.security.AuthorizationGuard;
import com.coffeeshop.api.service.AnalyticsService;
import com.coffeeshop.api.service.OrderStatusService;
import com.coffeeshop.api.service.PerformanceMetricsService;
import com.coffeeshop.api.websocket.WebSocketEventPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class OrderStatusServiceImpl implements OrderStatusService {


    private final OrderRepository orderRepository;
    private final AuthorizationGuard authorizationGuard;
    private static final ZoneId BUSINESS_TZ = ZoneId.of("Asia/Phnom_Penh");



    //----------------------------
    // Confirm Order
    //----------------------------
    @Override
    public void confirmAndSendToBarista(UUID orderId) {
        authorizationGuard.requireCashier();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getStatus() == OrderStatus.QUEUED
                || order.getStatus() == OrderStatus.PREPARING
                || order.getStatus() == OrderStatus.DONE
        )   {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order already confirmed");
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order must be in CREATED state before sending to Barista");
        }

        order.setStatus(OrderStatus.QUEUED);
        order.setConfirmedAt(ZonedDateTime.now(BUSINESS_TZ).toInstant());

        orderRepository.save(order);
    }





    //--------------------------
    // UPDATE ORDER STATUS
    //--------------------------
    @Transactional
    @Override
    public UpdateOrderStatusResponse updateOrderStatus(UUID orderId, String status) {
        User barista = authorizationGuard.requireBarista();

        if (orderId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order ID required");
        }
        if (status == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status required");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        OrderStatus oldStatus = order.getStatus();
        final OrderStatus requested;
        try{
            requested = OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + status);
        }

        if (order.getStatus() != OrderStatus.QUEUED && order.getStatus() != OrderStatus.PREPARING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order must be QUEUED or PREPARING");
        }

        if (oldStatus == requested) {
            return UpdateOrderStatusResponse.builder()
                    .orderId(order.getId())
                    .oldStatus(oldStatus)
                    .newStatus(order.getStatus())
                    .build();
        }

        Instant now = ZonedDateTime.now(BUSINESS_TZ).toInstant();
        switch (requested) {
            case PREPARING -> {
                if (oldStatus != OrderStatus.QUEUED) throw new ResponseStatusException(HttpStatus.CONFLICT, "Only from QUEUED.");
                order.setStatus(OrderStatus.PREPARING);

                if (order.getPreparationStartedAt() == null) order.setPreparationStartedAt(now);
                order.setProcessedBy(barista);
            }
            case DONE -> {
                if (oldStatus != OrderStatus.PREPARING) throw new ResponseStatusException(HttpStatus.CONFLICT, "Only from PREPARING.");
                order.setStatus(OrderStatus.DONE);
                order.setDoneAt(ZonedDateTime.now(BUSINESS_TZ).toInstant());
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PREPARING or DONE.");
        }

        order.setUpdatedAt(ZonedDateTime.now(BUSINESS_TZ).toInstant());
        Order saved = orderRepository.save(order);


        return UpdateOrderStatusResponse.builder()
                .orderId(saved.getId())
                .oldStatus(oldStatus)
                .newStatus(saved.getStatus())
                .build();
    }





    /**
     * Builds the order confirmation summary for a newly created order.
     *
     * The cashier uses this information to validate that the order was
     * successfully created and that all financial and item totals are
     * correct before continuing the workflow.
     *
     * Business Rules:
     * - Only cashiers are allowed to access this information.
     * - The order must exist; otherwise a 404 NOT_FOUND error is returned.
     * - Total units represent the sum of quantities across all order items.
     * - Total items represent the number of distinct order lines.
     *
     * @param orderId Unique identifier of the order.
     * @return Order summary for display on the cashier confirmation screen.
     * @throws ResponseStatusException If the order cannot be found.
     */
    @Override
    public GetCreatedOrderResponse getCreatedOrder(UUID orderId) {
        authorizationGuard.requireCashier();

        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")
        );

        Integer units = order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();

        return GetCreatedOrderResponse.builder()
                .orderId(orderId)
                .orderNumber(order.getOrderNumber())
                .paymentMethod(order.getPaymentMethod())
                .orderStatus(order.getStatus())
                .totalPrice(order.getTotalAmount())
                .totalItems(order.getItems().size())
                .totalUnits(units)
                .build();
    }
}