package com.coffeeshop.api.service.impl;

import com.coffeeshop.api.domain.Category;
import com.coffeeshop.api.dto.adminDashboard.report.ReportDashboardResponse;
import com.coffeeshop.api.repository.OrderRepository;
import com.coffeeshop.api.security.AuthorizationGuard;
import com.coffeeshop.api.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {


    private final AuthorizationGuard authorizationGuard;
    private final OrderRepository orderRepository;
    private static final ZoneId BUSINESS_TZ = ZoneId.of("Asia/Phnom_Penh");


    //----------------------
    // GET REPORTS
    //----------------------
    @Override
    public ReportDashboardResponse reports(Integer year, Integer month) {
        authorizationGuard.requireAdmin();

        YearMonth yearMonth = (year != null && month != null) ? YearMonth.of(year, month) : YearMonth.now();
        Instant start = yearMonth.atDay(1).atStartOfDay(BUSINESS_TZ).toInstant();
        Instant end = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay(BUSINESS_TZ).toInstant();

        YearMonth prev = yearMonth.minusMonths(1);
        Instant prevStart = prev.atDay(1).atStartOfDay(BUSINESS_TZ).toInstant();
        Instant prevEnd = prev.atEndOfMonth().plusDays(1).atStartOfDay(BUSINESS_TZ).toInstant();

        BigDecimal currRev = orderRepository.getTotalRevenue(start, end);
        BigDecimal prevRev = orderRepository.getTotalRevenue(prevStart, prevEnd);
        BigDecimal currProfit = orderRepository.getGrossProfit(start, end);
        BigDecimal prevProfit = orderRepository.getGrossProfit(prevStart, prevEnd);

        var summary = ReportDashboardResponse.Summary.builder()
                .grossProfit(buildMetric(currProfit, prevProfit))
                .netRevenue(buildMetric(currRev, prevRev))
                .build();

        return ReportDashboardResponse.builder()
                .summary(summary)
                .revenueTrends(buildRevenueTrends(yearMonth, start, end))
                .salesByCategory(buildSalesByCategory(start, end, currRev))
                .busiesHours(buildBusiestHours(start, end))
                .build();
    }


    // BUILD METRIC
    private ReportDashboardResponse.Metric buildMetric(BigDecimal cur, BigDecimal prev) {
        return ReportDashboardResponse.Metric.builder()
                .value(cur != null ? cur.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .growthPtc(calculateGrowth(cur, prev))
                .build();
    }


    // GROWTH CALCULATION
    private double calculateGrowth(BigDecimal cur, BigDecimal prev) {
        if (prev == null || prev.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        return cur.subtract(prev).divide(prev, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
    }


    // BUILD REVENUE TRENDS
    private List<BigDecimal> buildRevenueTrends(YearMonth ym, Instant start, Instant end) {
        List<Object[]> rows = orderRepository.getDailyRevenue(start, end);
        Map<LocalDate, BigDecimal> map = new HashMap<>();
        for (Object[] r : rows) {
            LocalDate d = ((java.sql.Date) r[0]).toLocalDate();
            BigDecimal v = (BigDecimal) r[1];
            map.put(d, v);
        }
        List<BigDecimal> list = new ArrayList<>();
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate cur = ym.atDay(1);
        for (int i = 1; i <= ym.lengthOfMonth(); i++) {
            BigDecimal v = map.getOrDefault(cur, BigDecimal.ZERO);
            if (cur.isAfter(today)) v = BigDecimal.ZERO;
            list.add(v.setScale(2, RoundingMode.HALF_UP));
            cur = cur.plusDays(1);
        }
        return list;
    }


    // BUILD SALES BY CATEGORY
    private List<ReportDashboardResponse.CategorySales> buildSalesByCategory(Instant s, Instant e, BigDecimal total) {
        List<Object[]> rows = orderRepository.getSalesByCategory(s, e);
        List<ReportDashboardResponse.CategorySales> list = new ArrayList<>();
        for (Object[] r : rows) {
            Category c = (Category) r[0];
            BigDecimal rev = (BigDecimal) r[1];
            int pct = (total != null && total.compareTo(BigDecimal.ZERO) > 0)
                    ? rev.multiply(BigDecimal.valueOf(100)).divide(total, 0, RoundingMode.HALF_UP).intValue()
                    : 0;
            list.add(ReportDashboardResponse.CategorySales.builder()
                    .label(c.getName()).percentage(pct).revenue(rev.setScale(2, RoundingMode.HALF_UP)).build());
        }
        return list;
    }


    // BUILD BUSIEST HOURS
    private List<List<Integer>> buildBusiestHours(Instant s, Instant e) {
        List<Object[]> rows = orderRepository.getHourlyDistribution(s, e);
        int[][] hours = new int[7][24];
        for (Object[] r : rows) {
            int wd = ((Number) r[0]).intValue();
            int hr = ((Number) r[1]).intValue();
            int cnt = ((Number) r[2]).intValue();
            if (wd >= 0 && wd < 7 && hr >= 0 && hr < 24) hours[wd][hr] = cnt;
        }
        List<List<Integer>> res = new ArrayList<>();
        for (int[] day : hours) {
            List<Integer> d = new ArrayList<>();
            for (int v : day) d.add(v);
            res.add(d);
        }
        return res;
    }


}
