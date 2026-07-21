package com.coffeeshop.api.service.impl;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.dto.adminDashboard.TimeRange;
import com.coffeeshop.api.dto.adminDashboard.report.*;
import com.coffeeshop.api.repository.OrderItemRepository;
import com.coffeeshop.api.repository.OrderRepository;
import com.coffeeshop.api.security.AuthorizationGuard;
import com.coffeeshop.api.service.ReportService;
import com.coffeeshop.api.util.DateRangeFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {


    private final AuthorizationGuard authorizationGuard;
    private final OrderRepository orderRepository;
    private static final ZoneId BUSINESS_TZ = ZoneId.of("Asia/Phnom_Penh");
    private final DateRangeFactory dateRangeFactory;
    private final OrderItemRepository orderItemRepository;



    // =====================================
    // GET Sales By Category
    // =====================================
    @Override
    public List<GetSalesByCategoryResponse> salesByCategory(TimeRange range) {
        authorizationGuard.requireAdmin();

        DateRangeFactory.DateRange getRange = dateRangeFactory.getDateRange(range);

        return orderItemRepository.findSalesByCategory(getRange.start(), getRange.end());
    }




    // ============================================================================
    // GET Busiest Hours
    // ============================================================================
    @Override
    public GetBusiestHoursResponse busiestHours() {
        authorizationGuard.requireAdmin();

        List<Order> orders = orderRepository.findAll();

        return buildHeatMap(orders);
    }

    private GetBusiestHoursResponse buildHeatMap(List<Order> orders) {

        Map<DayOfWeek, Map<Integer, Long>> grouped =
                orders.stream()
                        .collect(Collectors.groupingBy(
                                order -> order.getCreatedAt()
                                        .atZone(BUSINESS_TZ)
                                        .getDayOfWeek(),
                                Collectors.groupingBy(
                                        order -> order.getCreatedAt()
                                                .atZone(BUSINESS_TZ)
                                                .getHour(),
                                        Collectors.counting()
                                )
                        ));

        DayOfWeek[] daysOrder = {
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
        };

        List<GetBusiestHoursResponse.DayData> days =
                Arrays.stream(daysOrder)
                        .map(day -> {

                            Map<Integer, Long> hourly =
                                    grouped.getOrDefault(day, Map.of());

                            List<GetBusiestHoursResponse.TimeSlot> slots =
                                    IntStream.rangeClosed(6, 17) // 6AM -> 5PM
                                            .mapToObj(hour ->
                                                    GetBusiestHoursResponse.TimeSlot.builder()
                                                            .x(LocalTime.of(hour, 0)
                                                                    .format(DateTimeFormatter.ofPattern("ha")))
                                                            .y(hourly.getOrDefault(hour, 0L).intValue())
                                                            .build()
                                            )
                                            .toList();

                            return GetBusiestHoursResponse.DayData.builder()
                                    .id(day.name().substring(0, 3))
                                    .data(slots)
                                    .build();
                        })
                        .toList();

        return GetBusiestHoursResponse.builder()
                .days(days)
                .build();
    }



    // ==========================================
    // GET Revenue Trends
    // ==========================================
    @Override
    public List<RevenueTrendsResponse> revenueTrends(Integer year, Integer month) {
        authorizationGuard.requireAdmin();

        YearMonth yearMonth = (year != null & month != null) ? YearMonth.of(year, month) : YearMonth.now(BUSINESS_TZ);
        Instant start = yearMonth.atDay(1).atStartOfDay(BUSINESS_TZ).toInstant();
        Instant end = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay(BUSINESS_TZ).toInstant();

        List<DailyRevenueProjection> revenues = orderRepository.findDailyRevenue(start, end);

        Map<Integer, BigDecimal> revenueMap = revenues.stream()
                .collect(Collectors.toMap(
                        DailyRevenueProjection::getDay,
                        DailyRevenueProjection::getRevenue
                ));

        return IntStream.rangeClosed(1, yearMonth.lengthOfMonth())
                .mapToObj(day -> RevenueTrendsResponse.builder()
                        .day(String.valueOf(day))
                        .revenue(revenueMap.getOrDefault(day, BigDecimal.ZERO))
                        .build())
                .toList();
    }
}






