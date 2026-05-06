package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.adminDashboard.*;

public interface AdminDashboardService {

    BusinessAnalyticsSummaryResponse businessAnalyticsSummary ();

    TopSellingProductResponse topSellingProducts (TopSellingProductRequest request);

    ProductStockStatusResponse productStockStatus (int page, int size);

    GetAllStaffProfilesResponse getAllStaffProfiles (int page, int size);
}
