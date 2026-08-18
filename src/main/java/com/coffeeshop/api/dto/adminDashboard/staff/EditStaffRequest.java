package com.coffeeshop.api.dto.adminDashboard.staff;

import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.domain.enums.Schedule;
import com.coffeeshop.api.domain.enums.ShiftType;
import com.coffeeshop.api.domain.enums.Status;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record EditStaffRequest(

        @JsonProperty("name")
        String name,

        @JsonProperty("username")
        String username,

        @JsonProperty("password")
        String password,

        @JsonProperty("email")
        String email,

        @JsonProperty("role")
        Role role,

        @JsonProperty("status")
        Status status,

        @JsonProperty("shift_type")
        ShiftType shiftType,

        @JsonProperty("schedules")
        List<Schedule> schedules
) {
}
