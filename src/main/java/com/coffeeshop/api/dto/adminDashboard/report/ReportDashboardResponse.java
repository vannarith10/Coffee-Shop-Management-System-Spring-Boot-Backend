package com.coffeeshop.api.dto.adminDashboard.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record ReportDashboardResponse(

        @JsonProperty("summary")
        Summary summary,

        @JsonProperty("revenue_trends")
        List<BigDecimal> revenueTrends,

        @JsonProperty("sales_by_category")
        List<CategorySales> salesByCategory,

        @JsonProperty("busies_hours")
        List<List<Integer>> busiesHours

) {

    @Builder
    public record Summary (
            @JsonProperty("gross_profit")
            Metric grossProfit,

            @JsonProperty("net_revenue")
            Metric netRevenue
    ) {}

    @Builder
    public record Metric (
            @JsonProperty("value")
            BigDecimal value,

            @JsonProperty("growth_ptc")
            double growthPtc
    ) {}

    @Builder
    public record CategorySales (
            @JsonProperty("label")
            String label,

            @JsonProperty("percentage")
            int percentage,

            @JsonProperty("revenue")
            BigDecimal revenue
    ) {}

}
