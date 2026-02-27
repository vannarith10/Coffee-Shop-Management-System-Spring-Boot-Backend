package com.coffeeshop.api.dto;

import com.coffeeshop.api.domain.enums.Role;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record DisableUserResponse(

        String username,
        UUID userId,
        Role role,
        boolean status,
        Instant createdAt,
        Instant updatedAt

) {
}
