package com.coffeeshop.api.dto.adminDashboard.setting;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record GetShopProfile(

        @JsonProperty("name")
        String name,

        @JsonProperty("contact")
        String contact,

        @JsonProperty("address")
        String address,

        @JsonProperty("description")
        String description,

        @JsonProperty("image_url")
        String imageUrl,

        @JsonProperty("region")
        String region

) {
}
