package com.coffeeshop.api.dto.adminDashboard.staff;

import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.domain.enums.Schedule;
import com.coffeeshop.api.domain.enums.ShiftType;
import com.coffeeshop.api.domain.enums.Status;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.List;

@Builder
public record AddNewEmployeeRequest(

        @NotBlank
        @Size(max = 50)
        @JsonProperty("full_name")
        String fullName,

        @NotBlank
        @Size(min = 8, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$")
        @JsonProperty("username")
        String username,

        @NotBlank
        @Size(min = 8)
        @JsonProperty("password")
        String password,

        @NotNull
        @JsonProperty("role")
        Role role,

        @NotNull
        @JsonProperty("shift")
        ShiftType shift,

        @NotEmpty
        @JsonProperty("schedules")
        List<Schedule> schedules,

        @NotNull
        @JsonProperty("status")
        Status status
) {
}
