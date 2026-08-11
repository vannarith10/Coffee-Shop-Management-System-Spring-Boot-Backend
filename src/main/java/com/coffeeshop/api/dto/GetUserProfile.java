package com.coffeeshop.api.dto;

import com.coffeeshop.api.domain.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.UUID;

@Builder
public record GetUserProfile(

        @JsonProperty("user_id")
        UUID userId,

        @JsonProperty("username")
        String username,

        @JsonProperty("name")
        String name,

        @JsonProperty("image_url")
        String imageUrl,

        @JsonProperty("role")
        Role role

) {

}
