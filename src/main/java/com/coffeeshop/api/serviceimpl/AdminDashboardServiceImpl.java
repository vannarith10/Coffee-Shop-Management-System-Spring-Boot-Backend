package com.coffeeshop.api.serviceimpl;

import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.dto.adminDashboard.BusinessAnalyticsSummaryResponse;
import com.coffeeshop.api.dto.adminDashboard.TopSellingProductProjection;
import com.coffeeshop.api.dto.adminDashboard.TopSellingProductRequest;
import com.coffeeshop.api.dto.adminDashboard.TopSellingProductResponse;
import com.coffeeshop.api.minio.ImageStorageService;
import com.coffeeshop.api.repository.OrderItemRepository;
import com.coffeeshop.api.repository.OrderRepository;
import com.coffeeshop.api.repository.UserRepository;
import com.coffeeshop.api.service.AdminDashboardService;
import com.coffeeshop.api.service.UserService;
import com.coffeeshop.api.util.DateWindows;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {


    private final UserRepository userRepository;
    private final UserService userService;


    private static final ZoneId BUSINESS_TZ = ZoneId.of("Asia/Phnom_Penh");
    private static final OrderStatus DONE = OrderStatus.DONE;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ImageStorageService imageStorageService;



    // ====================================
    // Business Analytics Summary
    // ====================================
    @Override
    public BusinessAnalyticsSummaryResponse businessAnalyticsSummary() {

        // The first time of creating this method is for only ADMIN role, but now
        // I moved validate user role to the Controller place because this function call be called to use at Barista Update Status that has BARISTA role
        // Because Update Order Status handles real-time sending new Business Analytics Summary values
        // that it needs this function to calculate the values and unchanged return type

        ZonedDateTime nowBiz = ZonedDateTime.now(BUSINESS_TZ);
        var today = DateWindows.today(nowBiz);
        var yesterday = DateWindows.yesterday(nowBiz);


        BigDecimal todayRevenue = nvl (orderRepository.sumRevenueBetween(today.getStart(), today.getEnd(), DONE));
        long todayOrders = orderRepository.countOrdersBetween(today.getStart(), today.getEnd(), DONE);

        BigDecimal yRevenue = orderRepository.sumRevenueBetween(yesterday.getStart(), yesterday.getEnd(), DONE);
        long yOrders = orderRepository.countOrdersBetween(yesterday.getStart(), yesterday.getEnd(), DONE);


        BigDecimal todayAov = (todayOrders == 0)
                ? BigDecimal.ZERO
                : todayRevenue.divide(BigDecimal.valueOf(todayOrders), 2, RoundingMode.HALF_UP);

        BigDecimal yAov = (yOrders == 0)
                ? BigDecimal.ZERO
                : yRevenue.divide(BigDecimal.valueOf(yOrders), 2, RoundingMode.HALF_UP);

        double revGrowth = growthPct(todayRevenue, yRevenue);
        double ordGrowth = growthPct(BigDecimal.valueOf(todayOrders), BigDecimal.valueOf(yOrders));
        double aovGrowth = growthPct(todayAov, yAov);



        BusinessAnalyticsSummaryResponse.Summary summary = new BusinessAnalyticsSummaryResponse.Summary(
                new BusinessAnalyticsSummaryResponse.MetricResponse(safeMoney(todayRevenue), revGrowth),
                new BusinessAnalyticsSummaryResponse.MetricResponse((double) todayOrders, ordGrowth),
                new BusinessAnalyticsSummaryResponse.MetricResponse(safeMoney(todayAov), aovGrowth)
        );

        return new BusinessAnalyticsSummaryResponse(summary);
    }



    private static double growthPct(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        return current.subtract(previous)
                .divide(previous, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }


    // Helper functions
    private static double safeMoney(BigDecimal val) {
        return val.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
    private static BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }





    // =====================
    // Top Selling Products
    // =====================
    @Override
    public TopSellingProductResponse topSellingProducts(TopSellingProductRequest request) {
        // Validate User
        User user = userRepository.findById(userService.getCurrentUserId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        // Validate Role
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN role can get this resources.");
        }

        //
        ZoneId zone = ZoneId.systemDefault();
        Instant start;
        Instant end = Instant.now();
        switch (request.range()) {
            case TODAY -> {
                start = LocalDate.now(zone).atStartOfDay(zone).toInstant();
            }
            case THIS_WEEK -> {
                start = LocalDate.now(zone).with(DayOfWeek.MONDAY).atStartOfDay(zone).toInstant();
            }
            case THIS_MONTH -> {
                start = LocalDate.now(zone).withDayOfMonth(1).atStartOfDay(zone).toInstant();
            }
            case THIS_YEAR -> {
                start = LocalDate.now(zone).withDayOfYear(1).atStartOfDay(zone).toInstant();
            }
            case ALL -> {
                start = Instant.EPOCH;
            }

            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported range");
        }

        // Units target
        final int unitsTarget = 200;

        // Build Pageable
        int page = request.page() != null && request.page() >= 0
                ? request.page()
                : 0;

        // If request.size() < 10 -> return 10
        // If request.size() > 50 -> return 50
        int size = request.size() != null
                ? Math.clamp(request.size(), 10, 50)
                : 10;

        Pageable pageable = PageRequest.of(page, size);


        // Query repository
        Page<TopSellingProductProjection> projections = orderItemRepository
                .findTopSellingProductsByDateRange(
                    OrderStatus.DONE,
                    start,
                    end,
                    pageable
        );

        // Map Projections to response items
        List<TopSellingProductResponse.TopProductItem> productItems = projections.getContent()
                .stream()
                .map(product -> new TopSellingProductResponse.TopProductItem(
                        product.productId(),
                        product.productName(),
                        imageStorageService.getImageUrl(product.imageKey()),
                        Math.toIntExact(product.unitsSold())
                )).toList();

        // Build Response
        TopSellingProductResponse response = TopSellingProductResponse.builder()
                .unitsTarget(unitsTarget)
                .topProducts(productItems)
                .build();

        return response;
    }

























}






























