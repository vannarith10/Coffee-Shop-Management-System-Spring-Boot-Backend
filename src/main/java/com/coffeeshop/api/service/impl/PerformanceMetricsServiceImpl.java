package com.coffeeshop.api.service.impl;


import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.dto.order.PerformanceMetricsResponse;
import com.coffeeshop.api.repository.OrderRepository;
import com.coffeeshop.api.service.PerformanceMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PerformanceMetricsServiceImpl implements PerformanceMetricsService {


    private static final ZoneId BUSINESS_TZ = ZoneId.of("Asia/Phnom_Penh");
    private final OrderRepository orderRepository;


    // Find All Orders Completed Today
    //
    @Override
    public PerformanceMetricsResponse calculate(Duration slaTarget) {
        ZonedDateTime start = ZonedDateTime.now(BUSINESS_TZ).toLocalDate().atStartOfDay(BUSINESS_TZ);
        Instant from = start.toInstant();
        Instant to = start.plusDays(1).toInstant();

        List<Order> completed = orderRepository.findCompletedToday(from, to);
        int completedToday = completed.size();

        long totalSeconds = completed.stream().filter(o -> o.getPreparationStartedAt() != null)
                .mapToLong(o -> Duration.between(o.getPreparationStartedAt(), o.getDoneAt()).getSeconds())
                .filter(sec -> sec >= 0)
                .sum();

        String avgPrepTime;
        if (completedToday > 0 && totalSeconds > 0) {
            avgPrepTime = formatCompact(Duration.ofSeconds(totalSeconds / completedToday));
        } else {
            avgPrepTime = "0s";
        }

        long slaMet = completed.stream()
                .map(o -> Duration.between(o.getPreparationStartedAt(), o.getDoneAt()))
                .filter(d -> !d.isNegative() && !d.isZero())
                .filter(d -> d.compareTo(slaTarget) <= 0)
                .count();

        int efficiency = completedToday == 0 ? 0 : (int) Math.round((slaMet * 100.0) / completedToday);


        return PerformanceMetricsResponse.builder()
                .avg_prep_time(avgPrepTime)
                .completed_today(completedToday)
                .efficiency_percentage(efficiency)
                .build();
    }


    private String formatCompact(Duration d) {
        long sec = d.getSeconds();
        long h = sec / 3600;
        long m = (sec % 3600) / 60;
        long s = sec % 60;
        if (h > 0) return String.format("%dh %dm", h, m);
        if (m > 0) return String.format("%dm %ds", m, s);
        return String.format("%ds", s);
    }


}
