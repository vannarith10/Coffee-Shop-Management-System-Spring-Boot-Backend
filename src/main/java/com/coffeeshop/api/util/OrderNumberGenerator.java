package com.coffeeshop.api.util;

import com.coffeeshop.api.domain.Order;
import com.coffeeshop.api.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderNumberGenerator {

    private final OrderRepository orderRepository;

    @Transactional
    public String generate() {
        ZoneId cambodiaZone = ZoneId.of("Asia/Phnom_Penh");
        // Today in Cambodia zone
        LocalDate todayCambodia = LocalDate.now(cambodiaZone);
        // Start and end of day in Cambodia zone
        ZonedDateTime startOfDayCambodia = todayCambodia.atStartOfDay(cambodiaZone);
        ZonedDateTime endOfDayCambodia = todayCambodia.plusDays(1).atStartOfDay(cambodiaZone);
        // Convert to Instant (UTC) for DB query
        Instant startOfDayUtc = startOfDayCambodia.toInstant();
        Instant endOfDayUtc = endOfDayCambodia.toInstant();

        Optional<Order> lastOrderOpt =
                orderRepository.findTopByCreatedAtBetweenOrderByCreatedAtDesc(startOfDayUtc, endOfDayUtc);
        if (lastOrderOpt.isEmpty()) {
            return "A001";
        }
        String lastOrderNumber = lastOrderOpt.get().getOrderNumber();
        char letter = lastOrderNumber.charAt(0);
        int number = Integer.parseInt(lastOrderNumber.substring(1));
        if (number < 999) {
            number++;
        } else {
            number = 1;
            letter++;
        }
        if (letter > 'Z') {
            throw new IllegalStateException("Order number limit exceeded for today");
        }
        return letter + String.format("%03d", number);
        // Result: A001, A002,...B001,...Z999
    }
}