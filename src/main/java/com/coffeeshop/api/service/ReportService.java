package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.adminDashboard.report.ReportDashboardResponse;

public interface ReportService {

    ReportDashboardResponse reports (Integer year, Integer month);

}
