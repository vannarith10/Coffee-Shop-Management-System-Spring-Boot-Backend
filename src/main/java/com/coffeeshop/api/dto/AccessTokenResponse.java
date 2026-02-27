package com.coffeeshop.api.dto;

import java.time.Instant;

public record AccessTokenResponse(
        String accessToken,
        String tokenType,
        Long expiresIn,
        Refresh refresh

) {
    public record Refresh(
            String token,
            Instant expiresAt
    ) {}
}
