package com.coffeeshop.api.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RefreshAccessTokenRequest(

        @NotBlank
        @JsonProperty("refresh_token")
        String refreshToken

) {
}
