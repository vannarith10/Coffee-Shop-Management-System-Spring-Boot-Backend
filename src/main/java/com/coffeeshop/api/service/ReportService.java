package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.adminDashboard.TimeRange;
import com.coffeeshop.api.dto.adminDashboard.report.GetBusiestHoursResponse;
import com.coffeeshop.api.dto.adminDashboard.report.GetSalesByCategoryResponse;
import com.coffeeshop.api.dto.adminDashboard.report.RevenueTrendsResponse;

import java.util.List;

public interface ReportService {

    List<GetSalesByCategoryResponse> salesByCategory (TimeRange range);

    GetBusiestHoursResponse busiestHours ();

    List<RevenueTrendsResponse> revenueTrends (Integer year, Integer month);

}
