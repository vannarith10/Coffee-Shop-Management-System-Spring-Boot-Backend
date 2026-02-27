package com.coffeeshop.api.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record CashOrderResponse(

        @JsonProperty("order_id")
        UUID orderId,

        @JsonProperty("order_number")
        String orderNumber,

        @JsonProperty("status")
        String status,

        @JsonProperty("total_amount")
        BigDecimal totalAmount,

        @JsonProperty("note")
        String note,

        @JsonProperty("payment_method")
        String paymentMethod
){
}

