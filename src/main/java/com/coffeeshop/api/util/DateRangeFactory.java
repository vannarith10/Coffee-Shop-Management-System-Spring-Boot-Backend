package com.coffeeshop.api.util;


import com.coffeeshop.api.dto.adminDashboard.TimeRange;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

@Component
public class DateRangeFactory {

    private static final ZoneId BUSINESS_TZ = ZoneId.of("Asia/Phnom_Penh");

    public record DateRange (
            Instant start,
            Instant end
    ){};

    public DateRange getDateRange (TimeRange range) {
        LocalDate today = LocalDate.now(BUSINESS_TZ);

        return switch (range) {
            case TODAY -> new DateRange(
                    today.atStartOfDay(BUSINESS_TZ).toInstant(),
                    today.plusDays(1).atStartOfDay(BUSINESS_TZ).toInstant()
            );

            case THIS_WEEK -> {
                LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                yield new DateRange(
                        startOfWeek.atStartOfDay(BUSINESS_TZ).toInstant(),
                        startOfWeek.plusWeeks(1).atStartOfDay(BUSINESS_TZ).toInstant()
                );
            }

            case THIS_MONTH -> {
                LocalDate startOfMonth = today.withDayOfMonth(1);
                yield new DateRange(
                        startOfMonth.atStartOfDay(BUSINESS_TZ).toInstant(),
                        startOfMonth.plusMonths(1).atStartOfDay(BUSINESS_TZ).toInstant()
                );
            }

            case THIS_YEAR -> {
                LocalDate startOfYear = today.withDayOfYear(1);
                yield new DateRange(
                        startOfYear.atStartOfDay(BUSINESS_TZ).toInstant(),
                        startOfYear.plusYears(1).atStartOfDay(BUSINESS_TZ).toInstant()
                );
            }

            case ALL -> new DateRange(
                    Instant.EPOCH,
                    Instant.now()
            );
        };
    }

}
