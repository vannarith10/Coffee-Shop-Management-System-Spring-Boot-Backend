package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.adminDashboard.*;
import com.coffeeshop.api.dto.adminDashboard.product.GetAllProductsResponse;
import com.coffeeshop.api.dto.adminDashboard.staff.AddNewEmployeeRequest;
import com.coffeeshop.api.dto.adminDashboard.staff.AddNewEmployeeResponse;
import com.coffeeshop.api.dto.adminDashboard.staff.GetAllStaffProfilesResponse;

public interface AdminDashboardService {

    BusinessAnalyticsSummaryResponse businessAnalyticsSummary ();

    TopSellingProductResponse topSellingProducts (TopSellingProductRequest request);

    ProductStockStatusResponse productStockStatus (int page, int size);

    GetAllStaffProfilesResponse getAllStaffProfiles (int page, int size);

    AddNewEmployeeResponse addNewEmployee (AddNewEmployeeRequest request);

    GetAllProductsResponse getProducts (int page, int size);
}
