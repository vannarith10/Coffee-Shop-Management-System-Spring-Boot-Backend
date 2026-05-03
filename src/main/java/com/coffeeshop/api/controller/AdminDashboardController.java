package com.coffeeshop.api.controller;


import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.dto.adminDashboard.BusinessAnalyticsSummaryResponse;
import com.coffeeshop.api.dto.adminDashboard.TopSellingProductRequest;
import com.coffeeshop.api.dto.adminDashboard.TopSellingProductResponse;
import com.coffeeshop.api.repository.UserRepository;
import com.coffeeshop.api.service.AdminDashboardService;
import com.coffeeshop.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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



}
