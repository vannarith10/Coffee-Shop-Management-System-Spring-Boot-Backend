package com.coffeeshop.api.dto.adminDashboard.report;

import lombok.Builder;

import java.util.List;

@Builder
public record GetBusiestHoursResponse(

        List<DayData> days

) {

    @Builder
    public record DayData (
            String id, // MONDAY
            List<TimeSlot> data // {x: "6AM", y: 2}
    ){}

    @Builder
    public record TimeSlot (
            String x, // 6AM
            int y // 2 orders
    ){}

}
