package com.coffeeshop.api.dto.auth;

import com.coffeeshop.api.domain.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record RefreshAccessTokenResponse(

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        long expiresIn,

        @JsonProperty("refresh")
        Refresh refresh,

        @JsonProperty("user_info")
        UserInfo userInfo
) {

    @Builder
    public record UserInfo(
            @JsonProperty("id")
            UUID id,

            @JsonProperty("username")
            String username,

            @JsonProperty("role")
            Role role
    ) {}


    @Builder
    public record Refresh(
            @JsonProperty("token")
            String token,

            @JsonProperty("expires_at")
            Instant expiresAt
    ) {}
}
