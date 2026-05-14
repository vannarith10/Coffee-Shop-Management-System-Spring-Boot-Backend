package com.coffeeshop.api.dto.adminDashboard.setting;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record GetShopNameAndImage(

        @JsonProperty("name")
        String name,

        @JsonProperty("image_url")
        String imageUrl

) {
}
