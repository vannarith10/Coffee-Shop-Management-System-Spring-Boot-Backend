package com.coffeeshop.api.serviceimpl;

import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.OrderStatus;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.dto.adminDashboard.BusinessAnalyticsSummaryResponse;
import com.coffeeshop.api.repository.OrderRepository;
import com.coffeeshop.api.repository.UserRepository;
import com.coffeeshop.api.service.AdminDashboardService;
import com.coffeeshop.api.service.UserService;
import com.coffeeshop.api.util.DateWindows;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {


    private final UserRepository userRepository;
    private final UserService userService;


    private static final ZoneId BUSINESS_TZ = ZoneId.of("Asia/Phnom_Penh");
    private static final OrderStatus DONE = OrderStatus.DONE;
    private final OrderRepository orderRepository;


    @Override
    public BusinessAnalyticsSummaryResponse businessAnalyticsSummary() {
        // Get user
        User user = userRepository.findById(userService.getCurrentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        // Validate role
        if(user.getRole() != Role.ADMIN){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN role can get this resource.");
        }


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


    private static double safeMoney(BigDecimal val) {
        return val.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }


    private static BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }


}
