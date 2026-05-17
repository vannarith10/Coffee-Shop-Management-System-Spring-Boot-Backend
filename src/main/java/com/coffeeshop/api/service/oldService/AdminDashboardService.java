package com.coffeeshop.api.service.oldService;

import com.coffeeshop.api.domain.enums.ProductStock;
import com.coffeeshop.api.dto.adminDashboard.*;
import com.coffeeshop.api.dto.adminDashboard.product.AddNewProductRequest;
import com.coffeeshop.api.dto.adminDashboard.product.GetAllProductsResponse;
import com.coffeeshop.api.dto.adminDashboard.product.ProductStockStatusResponse;
import com.coffeeshop.api.dto.adminDashboard.product.UpdateProductRequest;
import com.coffeeshop.api.dto.adminDashboard.report.ReportDashboardResponse;
import com.coffeeshop.api.dto.adminDashboard.setting.GetShopNameAndImage;
import com.coffeeshop.api.dto.adminDashboard.setting.GetShopProfile;
import com.coffeeshop.api.dto.adminDashboard.setting.UpdateShopProfileRequest;
import com.coffeeshop.api.dto.adminDashboard.staff.AddNewEmployeeRequest;
import com.coffeeshop.api.dto.adminDashboard.staff.AddNewEmployeeResponse;
import com.coffeeshop.api.dto.adminDashboard.staff.EditStaffRequest;
import com.coffeeshop.api.dto.adminDashboard.staff.GetAllEmployeeProfilesResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface AdminDashboardService {

    BusinessAnalyticsSummaryResponse businessAnalyticsSummary ();

    TopSellingProductResponse topSellingProducts (TopSellingProductRequest request);

    ProductStockStatusResponse productStockStatus (int page, int size);

    GetAllEmployeeProfilesResponse getAllStaffProfiles (int page, int size);

    AddNewEmployeeResponse addNewEmployee (AddNewEmployeeRequest request, MultipartFile image);

    GetAllProductsResponse getProducts (int page, int size);

    void updateProductStockStatus (UUID productId, ProductStock newStockStatus);

    GetAllProductsResponse.ProductItem updateProductPartially (UUID productId, UpdateProductRequest request, MultipartFile file);

    GetAllProductsResponse.ProductItem addProduct (AddNewProductRequest request, MultipartFile image);

    ReportDashboardResponse reports (Integer year, Integer month);

    GetAllEmployeeProfilesResponse.Employee editStaffDetail (UUID id, EditStaffRequest request, MultipartFile image);

    GetShopProfile shopProfile ();

    GetShopProfile updateShopProfile (UpdateShopProfileRequest request, MultipartFile image);

    GetShopNameAndImage getShopNameAndImage ();
}
