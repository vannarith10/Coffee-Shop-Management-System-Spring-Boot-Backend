package com.coffeeshop.api.dto.order;


import lombok.Builder;

@Builder
public record PerformanceMetricsResponse(
        String avg_prep_time,

        int completed_today,

        int efficiency_percentage
) {
}
