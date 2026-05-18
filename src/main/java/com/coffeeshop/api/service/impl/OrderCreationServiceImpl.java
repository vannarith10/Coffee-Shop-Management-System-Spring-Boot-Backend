package com.coffeeshop.api.service.impl;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.domain.OrderItem;
import com.coffeeshop.api.domain.Product;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.domain.enums.PaymentMethod;
import com.coffeeshop.api.domain.enums.ProductStock;
import com.coffeeshop.api.dto.order.CashOrderResponse;
import com.coffeeshop.api.dto.order.CreateOrderRequest;
import com.coffeeshop.api.repository.OrderRepository;
import com.coffeeshop.api.repository.ProductRepository;
import com.coffeeshop.api.security.AuthorizationGuard;
import com.coffeeshop.api.service.OrderCreationService;
import com.coffeeshop.api.util.OrderNumberGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderCreationServiceImpl implements OrderCreationService {


    private final OrderRepository orderRepository;
    private final AuthorizationGuard authorizationGuard;
    private final OrderNumberGenerator orderNumberGenerator;
    private final ProductRepository productRepository;
    private static final Instant CAMBODIA_TIME_NOW = ZonedDateTime.now(ZoneId.of("Asia/Phnom_Penh")).toInstant();


    @Transactional
    @Override
    public CashOrderResponse createCashOrder(CreateOrderRequest request) {
        User cashier = authorizationGuard.requireCashier();

        if (request.items() == null || request.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must contain at least one item");
        }

        Order order = new Order();
        order.setOrderNumber(orderNumberGenerator.generate());
        order.setStatus(OrderStatus.CREATED);
        order.setPaymentMethod(PaymentMethod.CASH);
        order.setCreatedAt(CAMBODIA_TIME_NOW);
        order.setNote(request.note());
        order.setCreatedBy(cashier);


        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();


        // GO THROUGH EACH ORDER ITEM TO GET ITEM INFO
        for (CreateOrderRequest.OrderItem orderItem : request.items()) {
            // ORDER QUANTITY CHECK
            if (orderItem.quantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order item quantity must be greater than or equal 1");
            }

            // ORDER PRODUCT CHECK
            Product product = productRepository.findById(orderItem.productId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product item not found"));

            // AVAILABLE CHECK
            if (!product.isAvailable()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product item not available: " + product.getName());
            }

            // PRODUCT STOCK CHECK
            if (product.getStockStatus() == ProductStock.OUT_OF_STOCK) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product item out of stock");
            }

            // CATEGORY CHECK
            if (!product.getCategory().isActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not active: " + product.getCategory().getName());
            }

            // SELLING PRICE X QUANTITY
            BigDecimal originalLine = product.getPrice().multiply(BigDecimal.valueOf(orderItem.quantity()));

            BigDecimal unitPrice;
            BigDecimal lineTotal;

            if (product.isDiscount() && product.getDiscountRate() != null) {
                unitPrice = product.getPrice().multiply(BigDecimal.ONE.subtract(product.getDiscountRate())).setScale(2, RoundingMode.HALF_UP);
                lineTotal = unitPrice.multiply(BigDecimal.valueOf(orderItem.quantity())).setScale(2, RoundingMode.HALF_UP);
                discountTotal = discountTotal.add(originalLine.subtract(lineTotal)).setScale(2, RoundingMode.HALF_UP);
            }
            else
            {
                unitPrice = product.getPrice().setScale(2, RoundingMode.HALF_UP);
                lineTotal = originalLine.setScale(2, RoundingMode.HALF_UP);
            }

            total = total.add(lineTotal);
            subTotal = subTotal.add(originalLine);

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(orderItem.quantity())
                    .unitPrice(unitPrice)
                    .totalPrice(lineTotal)
                    .build();
            orderItems.add(item);
        }

        BigDecimal taxRate = new BigDecimal("0.00");
        BigDecimal taxAmount = total.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);

        total = total.add(taxAmount);

        order.setSubtotalAmount(subTotal);
        order.setDiscountAmount(discountTotal);
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(total);
        order.setItems(orderItems);

        Order saved = orderRepository.save(order);

        return CashOrderResponse.builder()
                .orderId(saved.getId())
                .orderNumber(saved.getOrderNumber())
                .status(saved.getStatus().toString())
                .totalAmount(saved.getTotalAmount())
                .note(saved.getNote())
                .paymentMethod(saved.getPaymentMethod().toString())
                .build();
    }

}
