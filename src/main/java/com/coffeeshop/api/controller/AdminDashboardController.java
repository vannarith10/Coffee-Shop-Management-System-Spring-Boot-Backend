package com.coffeeshop.api.controller;


import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.ProductStock;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.dto.adminDashboard.*;
import com.coffeeshop.api.dto.adminDashboard.product.GetAllProductsResponse;
import com.coffeeshop.api.dto.adminDashboard.staff.AddNewEmployeeRequest;
import com.coffeeshop.api.dto.adminDashboard.staff.AddNewEmployeeResponse;
import com.coffeeshop.api.dto.adminDashboard.staff.GetAllStaffProfilesResponse;
import com.coffeeshop.api.repository.UserRepository;
import com.coffeeshop.api.service.AdminDashboardService;
import com.coffeeshop.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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



}
