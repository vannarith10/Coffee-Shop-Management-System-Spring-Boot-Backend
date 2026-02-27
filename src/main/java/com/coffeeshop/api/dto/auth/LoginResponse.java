package com.coffeeshop.api.dto.auth;

import com.coffeeshop.api.domain.enums.Role;

import java.time.Instant;
import java.util.UUID;

public record LoginResponse(

        String accessToken,
        String tokenType,
        long expiresIn,
        Refresh refresh,
        UserInfo user

) {

    public record UserInfo(
            UUID id,
            String username,
            Role role
    ) {}

    public record Refresh(
            String token,
            Instant expiresAt
    ) {}

}
