package com.coffeeshop.api.dto.order;


import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.domain.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

// Usage: Used in cashier confirmation screen to retrieve order detail to confirm
@Builder
public record GetCreatedOrderResponse(

        @JsonProperty("order_id")
        UUID orderId,

        @JsonProperty("order_number")
        String orderNumber,

        @JsonProperty("payment_method")
        PaymentMethod paymentMethod,

        @JsonProperty("order_status")
        OrderStatus orderStatus,

        @JsonProperty("total_price")
        BigDecimal totalPrice,

        @JsonProperty("total_items")
        Integer totalItems,

        @JsonProperty("total_units")
        Integer totalUnits

) {
}
