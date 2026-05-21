package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.order.PerformanceMetricsResponse;

import java.time.Duration;

public interface PerformanceMetricsService {

    PerformanceMetricsResponse calculate (Duration slaTarget);

}
