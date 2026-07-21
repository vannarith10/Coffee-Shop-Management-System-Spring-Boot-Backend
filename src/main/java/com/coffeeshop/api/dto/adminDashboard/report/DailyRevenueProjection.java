package com.coffeeshop.api.dto.adminDashboard.report;

import lombok.Builder;

import java.math.BigDecimal;


// Used in Order Repo and Report Service Impl

@Builder
public record DailyRevenueProjection(

        Integer getDay,

        BigDecimal getRevenue

) {
}
