package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.adminDashboard.BusinessAnalyticsSummaryResponse;
import com.coffeeshop.api.dto.adminDashboard.TopSellingProductRequest;
import com.coffeeshop.api.dto.adminDashboard.TopSellingProductResponse;

public interface AdminDashboardService {

    BusinessAnalyticsSummaryResponse businessAnalyticsSummary ();

    TopSellingProductResponse topSellingProducts (TopSellingProductRequest request);

}
