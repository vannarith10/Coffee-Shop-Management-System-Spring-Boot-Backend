package com.coffeeshop.api.serviceimpl;


import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.domain.OrderItem;
import com.coffeeshop.api.domain.Product;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.domain.enums.PaymentMethod;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.dto.order.CashOrderResponse;
import com.coffeeshop.api.dto.order.CreateOrderRequest;
import com.coffeeshop.api.dto.order.OrderMessage;
import com.coffeeshop.api.mapper.OrderMapper;
import com.coffeeshop.api.repository.OrderRepository;
import com.coffeeshop.api.repository.ProductRepository;
import com.coffeeshop.api.repository.UserRepository;
import com.coffeeshop.api.service.OrderService;
import com.coffeeshop.api.service.UserService;
import com.coffeeshop.api.websocket.OrderEventPublisher;
import com.coffeeshop.api.util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final OrderNumberGenerator orderNumberGenerator;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;


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


        // Apply tax (example: 5%)
        BigDecimal taxRate = new BigDecimal("0.05");
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




    // Send Order To BARISTA
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
        if(order.getStatus() !=OrderStatus.CREATED){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order must be in CREATED state.");
        }

        // Update Order Status
        order.setStatus(OrderStatus.QUEUED);
        order.setConfirmedAt(Instant.now());
        Order saved = orderRepository.save(order);

        OrderMessage message = OrderMapper.toOrderMessage(saved);
        orderEventPublisher.sendToAllBaristas(message);

        return saved;
    }
}

