package com.coffeeshop.api.dto.order;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrderStatusRequest(

        @NotBlank(message = "Status is required")
        String status
) {
}
