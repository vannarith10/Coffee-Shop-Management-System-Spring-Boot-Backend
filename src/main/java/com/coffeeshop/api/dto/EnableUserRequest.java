package com.coffeeshop.api.dto;

import jakarta.validation.constraints.NotBlank;

public record EnableUserRequest(

        @NotBlank
        String username

) {
}
