package com.coffeeshop.api.serviceimpl;


import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.domain.OrderItem;
import com.coffeeshop.api.domain.Product;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.domain.enums.PaymentMethod;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.dto.adminDashboard.BusinessAnalyticsSummaryResponse;
import com.coffeeshop.api.dto.order.*;
import com.coffeeshop.api.mapper.OrderMapper;
import com.coffeeshop.api.minio.ImageStorageService;
import com.coffeeshop.api.repository.OrderRepository;
import com.coffeeshop.api.repository.ProductRepository;
import com.coffeeshop.api.repository.UserRepository;
import com.coffeeshop.api.service.AdminDashboardService;
import com.coffeeshop.api.service.OrderService;
import com.coffeeshop.api.service.UserService;
import com.coffeeshop.api.websocket.OrderEventPublisher;
import com.coffeeshop.api.util.OrderNumberGenerator;
import com.coffeeshop.api.websocket.WebSocketEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final OrderNumberGenerator orderNumberGenerator;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final WebSocketEventPublisher webSocketEventPublisher;
    private final AdminDashboardService adminDashboardService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ImageStorageService imageStorageService;


    // =============================
    // Create Order
    // =============================
    @Override
    @Transactional
    public CashOrderResponse createOrder(CreateOrderRequest request) {
        // Get current user
        UUID currentUser = userService.getCurrentUserId();

        // Get User
        User user = userRepository.findById(currentUser).orElseThrow(()->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );

        // Check role
        if(!user.getRole().equals(Role.CASHIER)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Allow only Cashier role!");
        }

        // Check request
        if(request.items() == null || request.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The order must at least contain one item!");
        }

        // Generate order number
        String odnum = orderNumberGenerator.generate();

        // Create an Order
        Order od = new Order();

        BigDecimal subTotalAmount = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxAmount;

        List<OrderItem> orderItems = new ArrayList<>();

        // Loop through each item
        for(CreateOrderRequest.OrderItem orderItem : request.items()){

            // Check order item quantity (must be at least 1 item)
            if(orderItem.quantity() <= 0){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Order item must contain at least 1.");
            }

            // Find product
            Product product = productRepository.findById(orderItem.productId()).orElseThrow(
                    ()->new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Product not found. \nID: " + orderItem.productId().toString())
            );

            // Check if product available
            if(!product.isAvailable()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Product is not available." + "\nProduct Name: " + product.getName());
            }

            // Check if category active
            if(!product.getCategory().isActive()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Category is not active." + "\nCategory Name: " + product.getCategory().getName());
            }

            // Price without discount
            BigDecimal originalLineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(orderItem.quantity()));


            // Price per unit
            BigDecimal unitPrice;
            // Total price for this item
            BigDecimal lineTotal;

            // DISCOUNT
            // Item price x quantity x discount (total amount)
            if(product.isDiscount() && product.getDiscountRate() != null){
                // UnitPrice = price * (1 - rate)
                unitPrice = product.getPrice()
                        .multiply(BigDecimal.ONE.subtract(product.getDiscountRate()))
                        .setScale(2, RoundingMode.HALF_UP);

                // LineTotal = UnitPrice * Quantity
                lineTotal = unitPrice.multiply(BigDecimal.valueOf(orderItem.quantity()))
                        .setScale(2, RoundingMode.HALF_UP);

                // DiscountAmount = OriginalPrice - DiscountPrice
                BigDecimal lineDiscountAmount = originalLineTotal.subtract(lineTotal);
                lineDiscountAmount = lineDiscountAmount.setScale(2, RoundingMode.HALF_UP);
                discountAmount = discountAmount.add(lineDiscountAmount);

            }else{
                // NO DISCOUNT
                lineTotal = originalLineTotal.setScale(2, RoundingMode.HALF_UP);
                unitPrice = product.getPrice().setScale(2, RoundingMode.HALF_UP);
            }

            // Add to total
            totalAmount = totalAmount.add(lineTotal);
            subTotalAmount = subTotalAmount.add(originalLineTotal);



            // Build OrderItem
            OrderItem item = new OrderItem();
            item.setOrder(od);
            item.setProduct(product);
            item.setQuantity(orderItem.quantity());
            item.setUnitPrice(unitPrice);
            item.setTotalPrice(lineTotal);

            // Add to list
            orderItems.add(item);
        }


        // Apply tax (example: 0%)
        BigDecimal taxRate = new BigDecimal("0.00");
        taxAmount = totalAmount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        totalAmount = totalAmount.add(taxAmount);



        // Set Order values
        od.setOrderNumber(odnum);
        od.setStatus(OrderStatus.CREATED);
        od.setSubtotalAmount(subTotalAmount);
        od.setDiscountAmount(discountAmount);
        od.setTaxAmount(taxAmount);
        od.setTotalAmount(totalAmount);
        od.setPaymentMethod(PaymentMethod.CASH);
        od.setCreatedAt(Instant.now());
        od.setNote(request.note());
        od.setCreatedBy(user);
        od.setItems(orderItems);

        Order odSave = orderRepository.save(od);

        return CashOrderResponse.builder()
                .orderId(odSave.getId())
                .orderNumber(odSave.getOrderNumber())
                .status(odSave.getStatus().toString())
                .totalAmount(odSave.getTotalAmount())
                .note(odSave.getNote())
                .paymentMethod(odSave.getPaymentMethod().toString())
                .build();
    }




    // ==================================
    // Confirm Order and send To BARISTA
    // ==================================
    @Override
    public Order confirmAndSendToBarista(UUID orderId) {

        // Get User
        UUID userId = userService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        // Validate ROLE
        if(user.getRole() != Role.CASHIER){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only CASHIER can confirm the Order.");
        }

        // Get Order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found."));

        // Check if order has already sent or not
        if(order.getStatus() == OrderStatus.QUEUED
                || order.getStatus() == OrderStatus.PREPARING
                || order.getStatus() == OrderStatus.DONE){
            return order;
        }

        // Validate Order status (It's must be CREATED)
        if(order.getStatus() != OrderStatus.CREATED){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order must be in CREATED state.");
        }

        // Update Order Status
        order.setStatus(OrderStatus.QUEUED);
        order.setConfirmedAt(Instant.now());
        Order saved = orderRepository.save(order);



        // WebSocket: Publish new Order to Barista
        OrderMessageToBarista message = OrderMapper.toOrderMessage(saved , imageStorageService);
        Object event = Map.of("event", "new.order",
                "payload", message);
        webSocketEventPublisher.publishToBarista(event);


        return saved;
    }



    // ======================================
    // Update Order Status (BARISTA)
    // ======================================
    @Transactional
    @Override
    public UpdateOrderStatusResponse updateOrderStatus(UUID orderId, String status) {
        // Order Status road: CREATED -> QUEUE -> PREPARING -> DONE
        // BARISTA can turn to only PREPARING or DONE at a time

        // Validate Inputs
        if (orderId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order ID is required.");
        }
        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required.");
        }

        // Get user
        User user = userRepository.findById(userService.getCurrentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        // Validate role
        if (user.getRole() != Role.BARISTA) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only BARISTA can update order status.");
        }

        // Get order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found."));

        OrderStatus oldStatus = order.getStatus();


        // Parse requested
        final OrderStatus requested;
        try {
            requested = OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + status);
        }


        // Check Order Status (Must be in QUEUED state)
        if (order.getStatus() != OrderStatus.QUEUED && order.getStatus() != OrderStatus.PREPARING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order status must in QUEUE or PREPARING state. (Current status: " + oldStatus + ").");
        }


        // Idempotent: if already the requested status, return OK
        if (oldStatus == requested) {
            return UpdateOrderStatusResponse.builder()
                    .orderId(order.getId())
                    .oldStatus(oldStatus)
                    .newStatus(order.getStatus())
                    .build();
        }

        Instant now = Instant.now();
        switch (requested) {
            case PREPARING -> {
                if (oldStatus != OrderStatus.QUEUED) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Can move to PREPARING only from QUEUED (current: " + oldStatus + ").");
                }
                order.setStatus(OrderStatus.PREPARING);
                if (order.getPreparationStartedAt() == null) order.setPreparationStartedAt(now);
                order.setProcessedBy(user);
            }
            case DONE -> {
                if (oldStatus != OrderStatus.PREPARING) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Can move to DONE only from PREPARING (current: " + oldStatus + ").");
                }
                order.setStatus(OrderStatus.DONE);
                order.setDoneAt(now);
                order.setProcessedBy(user);
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "BARISTA can only set status to PREPARING or DONE.");
        }



        // Update Order fields
        order.setUpdatedAt(now);
        Order saved = orderRepository.save(order);

        // WebSocket: Send to subscriber (Admin Dashboard Summary)
        OrderEvent event = new OrderEvent(
                "order.update.status",
                saved.getId(),
                Map.of(
                        "old_status", oldStatus.toString(),
                        "new_status", saved.getStatus().toString()
                )
        );

        // Send to Barista himself to update UI
        // Publish AFTER COMMIT to avoid ghost updates
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                webSocketEventPublisher.publishToBarista(event);
            }
        });


        // Admin Summary Analytics real-time update
        if (saved.getStatus() == OrderStatus.DONE) {
            // Publish AFTER COMMIT to avoid ghost updates
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    BusinessAnalyticsSummaryResponse summary = adminDashboardService.businessAnalyticsSummary();
                    simpMessagingTemplate.convertAndSend("/topic/admin-dashboard/summary", summary);
                }
            });
        }

        return UpdateOrderStatusResponse.builder()
                .orderId(saved.getId())
                .oldStatus(oldStatus)
                .newStatus(saved.getStatus())
                .build();
    }




    // =============================
    // Barista gets Orders
    // =============================
    @Override
    public List<OrderMessageToBarista> findRecentVisibleOrders() {

        // This one has a bug, it returns only 50 oldest orders
//        EnumSet<OrderStatus> visible = EnumSet.of(
//                OrderStatus.QUEUED,
//                OrderStatus.PREPARING,
//                OrderStatus.DONE);
//        List<Order> orders = orderRepository.findTop50ByStatusInOrderByCreatedAtAsc(visible);


        // Calculate cutoff time (7 Days)
        Instant cutoffTime = Instant.now().minus(7, ChronoUnit.DAYS);

        // Get active statuses
        EnumSet<OrderStatus> activeStatuses = EnumSet.of(
                OrderStatus.QUEUED,
                OrderStatus.PREPARING
        );

        // Fetch orders
        List<Order> orders = orderRepository.findVisibleOrders(
                activeStatuses,
                cutoffTime
        );

        return orders.stream().map(order ->
                        OrderMapper.toOrderMessage(order , imageStorageService))
                .toList();
    }



























}

