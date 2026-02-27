package com.coffeeshop.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DisableUserRequest(

        @NotBlank
        String username

) {
}
