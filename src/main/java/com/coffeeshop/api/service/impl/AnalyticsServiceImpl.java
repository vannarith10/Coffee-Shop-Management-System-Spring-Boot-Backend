package com.coffeeshop.api.service.impl;

import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.dto.Pagination;
import com.coffeeshop.api.dto.adminDashboard.*;
import com.coffeeshop.api.helper.MoneyHelper;
import com.coffeeshop.api.helper.PaginationHelper;
import com.coffeeshop.api.minio.ImageStorageService;
import com.coffeeshop.api.repository.OrderItemRepository;
import com.coffeeshop.api.repository.OrderRepository;
import com.coffeeshop.api.security.AuthorizationGuard;
import com.coffeeshop.api.service.AnalyticsService;
import com.coffeeshop.api.util.DateWindows;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final ZoneId BUSINESS_TZ = ZoneId.of("Asia/Phnom_Penh");
    private static final OrderStatus DONE = OrderStatus.DONE;
    private static final Instant CAMBODIA_TIME_NOW = ZonedDateTime.now(ZoneId.of("Asia/Phnom_Penh")).toInstant();

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ImageStorageService imageStorageService;
    private final AuthorizationGuard authorizationGuard;




    //----------------------------
    // BUSINESS ANALYTICS
    //----------------------------
    @Override
    public BusinessAnalyticsSummaryResponse businessAnalyticsSummary() {
        ZonedDateTime nowBiz = ZonedDateTime.now(BUSINESS_TZ);
        var today = DateWindows.today(nowBiz);
        var yesterday = DateWindows.yesterday(nowBiz);

        BigDecimal todayRevenue = MoneyHelper.nullToZero(orderRepository.sumRevenueBetween(today.getStart(), today.getEnd(), DONE));
        BigDecimal yesterdayRevenue = MoneyHelper.nullToZero(orderRepository.sumRevenueBetween(yesterday.getStart(), yesterday.getEnd(), DONE));
        long todayOrders = orderRepository.countOrdersBetween(today.getStart(), today.getEnd(), DONE);
        long yesterdayOrders = orderRepository.countOrdersBetween(yesterday.getStart(), yesterday.getEnd(), DONE);

        BigDecimal todayAov = (todayOrders == 0) ? BigDecimal.ZERO : todayRevenue.divide(BigDecimal.valueOf(todayOrders), 2, RoundingMode.HALF_UP);
        BigDecimal yesterdayAov = (yesterdayOrders == 0) ? BigDecimal.ZERO : yesterdayRevenue.divide(BigDecimal.valueOf(yesterdayOrders), 2, RoundingMode.HALF_UP);

        double revenueGrowth = MoneyHelper.growthPct(todayRevenue, yesterdayRevenue);
        double orderGrowth = MoneyHelper.growthPct(BigDecimal.valueOf(todayOrders), BigDecimal.valueOf(yesterdayOrders));
        double aovGrowth = MoneyHelper.growthPct(todayAov, yesterdayAov);

        var summary = BusinessAnalyticsSummaryResponse.Summary.builder()
                .todayRevenue(BusinessAnalyticsSummaryResponse.MetricResponse.builder()
                        .value(MoneyHelper.safeMoney(todayRevenue))
                        .growthPct(revenueGrowth)
                        .build())
                .todayTotalOrders(BusinessAnalyticsSummaryResponse.MetricResponse.builder()
                        .value((double) todayOrders)
                        .growthPct(orderGrowth)
                        .build())
                .todayAverageOrderValue(BusinessAnalyticsSummaryResponse.MetricResponse.builder()
                        .value(MoneyHelper.safeMoney(todayAov))
                        .growthPct(aovGrowth)
                        .build())
                .build();

        return BusinessAnalyticsSummaryResponse.builder()
                .summary(summary)
                .build();
    }




    //------------------------------
    // TOP SELLING PRODUCTS
    //------------------------------
    @Override
    public TopSellingProductResponse topSellingProducts(TopSellingProductRequest request) {
        authorizationGuard.requireAdmin();

        Instant end = CAMBODIA_TIME_NOW;
        Instant start = resolveStart(request.range(), BUSINESS_TZ);

        int unitsTarget = 100;

        Pageable pageable = PaginationHelper.of(request.page(), request.size());

        Page<TopSellingProductProjection> projections = orderItemRepository.findTopSellingProductsByDateRange(DONE, start, end, pageable);

        var items = projections.getContent().stream()
                .map(pro -> TopSellingProductResponse.TopProductItem.builder()
                        .productId(pro.productId())
                        .productName(pro.productName())
                        .imageUrl(imageStorageService.getImageUrl(pro.imageKey()))
                        .unitsSold(Math.toIntExact(pro.unitsSold()))
                        .build()).toList();

        var pagination = Pagination.builder()
                .page(pageable.getPageNumber() + 1)
                .size(pageable.getPageSize())
                .totalPages(projections.getTotalPages())
                .totalItems(projections.getTotalElements())
                .build();


        return TopSellingProductResponse.builder()
                .pagination(pagination)
                .unitsTarget(unitsTarget)
                .topProducts(items)
                .build();
    }




    // Helper
    private Instant resolveStart(TopSellingProductTimeRange range, ZoneId zone) {
        LocalDate now = LocalDate.now(zone);
        return switch (range) {
            case TODAY -> now.atStartOfDay(zone).toInstant();
            case THIS_WEEK -> now.with(DayOfWeek.MONDAY).atStartOfDay(zone).toInstant();
            case THIS_MONTH -> now.withDayOfMonth(1).atStartOfDay(zone).toInstant();
            case THIS_YEAR -> now.withDayOfYear(1).atStartOfDay(zone).toInstant();
            case ALL -> Instant.EPOCH;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported range");
        };
    }




}






