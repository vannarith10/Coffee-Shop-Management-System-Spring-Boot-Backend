package com.coffeeshop.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank
        String username,

        @NotBlank
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        String password,

        @NotBlank
        @Size(min = 1, max = 10, message = "Role must not be less than 1 or more than 10")
        String role

) {
}
