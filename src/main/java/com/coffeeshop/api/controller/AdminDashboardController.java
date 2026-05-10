package com.coffeeshop.api.controller;


import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.ProductStock;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.dto.adminDashboard.*;
import com.coffeeshop.api.dto.adminDashboard.product.AddProductRequest;
import com.coffeeshop.api.dto.adminDashboard.product.GetAllProductsResponse;
import com.coffeeshop.api.dto.adminDashboard.product.UpdateProductRequest;
import com.coffeeshop.api.dto.adminDashboard.staff.AddNewEmployeeRequest;
import com.coffeeshop.api.dto.adminDashboard.staff.AddNewEmployeeResponse;
import com.coffeeshop.api.dto.adminDashboard.staff.GetAllStaffProfilesResponse;
import com.coffeeshop.api.repository.UserRepository;
import com.coffeeshop.api.service.AdminDashboardService;
import com.coffeeshop.api.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin-dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final UserRepository userRepository;
    private final UserService userService;



    // Summary
    @GetMapping("/summary")
    public ResponseEntity<BusinessAnalyticsSummaryResponse> businessAnalyticsSummary () {
        // Get user
        User user = userRepository.findById(userService.getCurrentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        // Validate role
        if(user.getRole() != Role.ADMIN){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN role can get this resource.");
        }
        return ResponseEntity.ok(adminDashboardService.businessAnalyticsSummary());
    }



    // Get Top Selling Products
    @PostMapping("/top-selling-products")
    public ResponseEntity<TopSellingProductResponse> getTopSellingProduct (@Valid @RequestBody TopSellingProductRequest request) {
        TopSellingProductResponse response = adminDashboardService.topSellingProducts(request);
        return ResponseEntity.ok(response);
    }



    // Get All Products Statuses
    @GetMapping("/products-statuses")
    public ResponseEntity<ProductStockStatusResponse> productStockStatus (
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ProductStockStatusResponse response = adminDashboardService.productStockStatus(page, size);
        return ResponseEntity.ok(response);
    }



    // Get All Staff Profiles Info
    @GetMapping("/staff-profiles")
    public ResponseEntity<GetAllStaffProfilesResponse> getStaffProfiles (
            @RequestParam(defaultValue = "1")
            int page,
            @RequestParam(defaultValue = "10")
            int size
    ) {
        GetAllStaffProfilesResponse response = adminDashboardService.getAllStaffProfiles(page, size);
        return ResponseEntity.ok(response);
    }



    @PostMapping("/create-employee-account")
    public ResponseEntity<AddNewEmployeeResponse> addNewEmployee (
            @Valid @RequestBody
            AddNewEmployeeRequest request
    ) {
        AddNewEmployeeResponse response = adminDashboardService.addNewEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @GetMapping("/get-all-products")
    public ResponseEntity<GetAllProductsResponse> getProducts (
            @RequestParam int page, @RequestParam int size
    ) {
        GetAllProductsResponse response = adminDashboardService.getProducts(page, size);
        return ResponseEntity.ok(response);
    }



    @PutMapping("/product/{id}/stock-status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProductStockStatus (
            @PathVariable UUID id,
            @RequestParam ProductStock status
            ) {
        adminDashboardService.updateProductStockStatus(id, status);
    }



    //
    //  UPDATE PRODUCT
    //
    @PatchMapping(value = "/product/{id}/patch-product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GetAllProductsResponse.ProductItem> updateProductPartially (
            @PathVariable UUID id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category_name", required = false) String categoryName,
            @RequestParam(value = "selling_price", required = false) BigDecimal sellingPrice,
            @RequestParam(value = "cost_price", required = false) BigDecimal costPrice,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile file
            ) {
        UpdateProductRequest request = new UpdateProductRequest(name, categoryName, sellingPrice, costPrice, description);
        GetAllProductsResponse.ProductItem response = adminDashboardService.updateProductPartially(id, request, file);
        return ResponseEntity.ok(response);
    }




    // Use @RequestParam for text fields and files.
    // Use @RequestPart only when a part contains structured content such as JSON.
    //
    //  ADD NEW PRODUCT
    //
    @PostMapping(value = "/add-product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GetAllProductsResponse.ProductItem> addNewProduct (
            @RequestParam("name") @NotBlank String name,
            @RequestParam("price") BigDecimal price,
            @RequestParam("cost") BigDecimal cost,
            @RequestParam("category_name") String categoryName,
            @RequestParam("stock_status") String stockStatus,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        AddProductRequest request = AddProductRequest.builder()
                .name(name)
                .sellingPrice(price)
                .costPrice(cost)
                .categoryName(categoryName)
                .stockStatus(stockStatus)
                .description(description)
                .build();
        GetAllProductsResponse.ProductItem response = adminDashboardService.addProduct(request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}
