package com.coffeeshop.api.util;


import lombok.Value;

import java.time.Instant;
import java.time.ZonedDateTime;

public class DateWindows {

    @Value
    public static class Window {
        Instant start;
        Instant end;
    }

    public static Window today(ZonedDateTime nowBusinessTz) {
        ZonedDateTime start = nowBusinessTz.toLocalDate().atStartOfDay(nowBusinessTz.getZone());
        ZonedDateTime end = start.plusDays(1);
        return new Window(start.toInstant(), end.toInstant());
    }

    public static Window yesterday(ZonedDateTime nowBusinessTz) {
        ZonedDateTime start = nowBusinessTz.toLocalDate().minusDays(1).atStartOfDay(nowBusinessTz.getZone());
        ZonedDateTime end = start.plusDays(1);
        return new Window(start.toInstant(), end.toInstant());
    }

}
