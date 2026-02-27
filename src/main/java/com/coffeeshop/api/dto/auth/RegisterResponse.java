package com.coffeeshop.api.dto.auth;

import com.coffeeshop.api.domain.enums.Role;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record RegisterResponse(

        UUID id,
        String username,
        Role role,
        boolean active,
        Instant createdAt

) {
}
