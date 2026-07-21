package com.coffeeshop.api.controller;


import com.coffeeshop.api.dto.adminDashboard.TimeRange;
import com.coffeeshop.api.dto.adminDashboard.report.GetBusiestHoursResponse;
import com.coffeeshop.api.dto.adminDashboard.report.GetSalesByCategoryResponse;
import com.coffeeshop.api.dto.adminDashboard.report.RevenueTrendsResponse;
import com.coffeeshop.api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;


    // -------------------------------------------------
    // Get Sales By Category
    // -------------------------------------------------
    @GetMapping("/sales-by-category/{range}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GetSalesByCategoryResponse>> getSalesByCategory (
            @PathVariable TimeRange range
    ) {
       return ResponseEntity.ok(reportService.salesByCategory(range));
    }



    // ----------------------------------------------------
    // Busiest Hours
    // ----------------------------------------------------
    @GetMapping("/busiest-hours")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GetBusiestHoursResponse> busiestHours () {
        return ResponseEntity.ok(reportService.busiestHours());
    }




    // ---------------------------------------------------
    // Revenue Trends
    // ---------------------------------------------------
    @GetMapping("/revenue-trends/{month}/{year}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RevenueTrendsResponse>> revenueTrends (
            @PathVariable Integer year,
            @PathVariable Integer month
    ) {
        return ResponseEntity.ok(reportService.revenueTrends(year, month));
    }

}
