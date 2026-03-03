package com.coffeeshop.api.dto.order;

import com.coffeeshop.api.domain.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UpdateOrderStatusResponse(

        @JsonProperty("order_id")
        UUID orderId,

        @JsonProperty("old_status")
        OrderStatus oldStatus,

        @JsonProperty("new_status")
        OrderStatus newStatus

) {
}
