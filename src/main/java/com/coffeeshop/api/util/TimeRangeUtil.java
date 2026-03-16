package com.coffeeshop.api.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeRangeUtil {

    public static class MonthRange {
        public final Instant startInclusive;
        public final Instant endExclusive;

        public MonthRange(Instant startInclusive, Instant endExclusive) {
            this.startInclusive = startInclusive;
            this.endExclusive = endExclusive;
        }
    }

    public static MonthRange currentMonthRangePhnomPenh() {
        ZoneId khZone = ZoneId.of("Asia/Phnom_Penh");
        ZonedDateTime nowKh = ZonedDateTime.now(khZone);

        ZonedDateTime startOfMonthKh = nowKh.withDayOfMonth(1).toLocalDate().atStartOfDay(khZone);
        ZonedDateTime startOfNextMonthKh = startOfMonthKh.plusMonths(1);

        return new MonthRange(startOfMonthKh.toInstant(), startOfNextMonthKh.toInstant());
    }

}
