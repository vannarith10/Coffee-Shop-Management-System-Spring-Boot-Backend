package com.coffeeshop.api.dto.adminDashboard.staff;

import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.domain.enums.Schedule;
import com.coffeeshop.api.domain.enums.ShiftType;
import com.coffeeshop.api.domain.enums.Status;
import com.coffeeshop.api.dto.Pagination;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record GetAllEmployeeProfilesResponse(

        @JsonProperty("message")
        String message,

        @JsonProperty("pagination")
        Pagination pagination,

        @JsonProperty("staffs")
        List<Employee> staffs
) {

    @Builder
    public record Employee(
            @JsonProperty("id")
            UUID id,

            @JsonProperty("name")
            String name,

            @JsonProperty("username")
            String username,

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

}
