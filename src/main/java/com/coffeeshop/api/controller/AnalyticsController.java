package com.coffeeshop.api.controller;


import com.coffeeshop.api.dto.adminDashboard.BusinessAnalyticsSummaryResponse;
import com.coffeeshop.api.dto.adminDashboard.TopSellingProductRequest;
import com.coffeeshop.api.dto.adminDashboard.TopSellingProductResponse;
import com.coffeeshop.api.service.AnalyticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin-dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;



    // ANALYTICS SUMMARY
    @GetMapping("/summary")
    public ResponseEntity<BusinessAnalyticsSummaryResponse> summary () {
        return ResponseEntity.ok(analyticsService.businessAnalyticsSummary());
    }


    // TOP SELLING PRODUCT
    @PostMapping("/top-selling-products")
    public ResponseEntity<TopSellingProductResponse> topSelling (
            @Valid
            @RequestBody
            TopSellingProductRequest request) {
        return ResponseEntity.ok(analyticsService.topSellingProducts(request));
    }

}
