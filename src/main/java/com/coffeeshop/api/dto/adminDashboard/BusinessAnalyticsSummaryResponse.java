package com.coffeeshop.api.dto.adminDashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record BusinessAnalyticsSummaryResponse(
        Summary summary
) {
    public record Summary (
            @JsonProperty("today_revenue")
            MetricResponse todayRevenue,

            @JsonProperty("today_total_orders")
            MetricResponse todayTotalOrders,

            @JsonProperty("today_average_order_value")
            MetricResponse todayAverageOrderValue
    ) {}

    public record MetricResponse (
            @JsonProperty("value")
            double value,

            @JsonProperty("growth_pct")
            double growthPct
    ) {}
}
