package com.coffeeshop.api.dto.adminDashboard;

import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.domain.enums.Schedule;
import com.coffeeshop.api.domain.enums.ShiftType;
import com.coffeeshop.api.domain.enums.Status;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record GetAllStaffProfilesResponse(

        @JsonProperty("message")
        String message,

        @JsonProperty("pagination")
        Pagination pagination,

        @JsonProperty("staffs")
        List<Staff> staffs
) {

    @Builder
    public record Staff (
            @JsonProperty("id")
            UUID id,

            @JsonProperty("name")
            String name,

            @JsonProperty("role")
            Role role,

            @JsonProperty("shift")
            ShiftType shift,

            @JsonProperty("schedules")
            List<Schedule> schedules,

            @JsonProperty("email")
            String email,

            @JsonProperty("phone_number")
            String phoneNumber,

            @JsonProperty("status")
            Status status,

            @JsonProperty("image_url")
            String imageUrl
    ) {}


    @Builder
    public record Pagination (
            @JsonProperty("page")
            Integer page,

            @JsonProperty("size")
            Integer size,

            @JsonProperty("total_pages")
            Integer totalPages,

            @JsonProperty("total_items")
            Long totalItems
    ) {}
}
