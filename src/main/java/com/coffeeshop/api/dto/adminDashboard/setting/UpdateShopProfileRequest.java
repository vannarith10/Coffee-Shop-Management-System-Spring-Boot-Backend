package com.coffeeshop.api.dto.adminDashboard.setting;

import jakarta.validation.constraints.Size;

public record UpdateShopProfileRequest(

        @Size(max = 100)
        String name,

        @Size(max = 100)
        String contact,

        @Size(max = 150)
        String address,

        @Size(max = 250)
        String description,

        @Size(max = 50)
        String region

) {
}
