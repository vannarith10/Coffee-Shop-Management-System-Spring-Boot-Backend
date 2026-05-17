package com.coffeeshop.api.mapper;

import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.dto.adminDashboard.staff.GetAllEmployeeProfilesResponse;
import com.coffeeshop.api.minio.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final ImageStorageService imageStorageService;


    // MAP FROM USER TO EMPLOYEE RESPONSE
    public GetAllEmployeeProfilesResponse.Employee toEmployeeResponseDto (User user) {
        return GetAllEmployeeProfilesResponse.Employee
                .builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .role(user.getRole())
                .shift(user.getShiftType())
                .schedules(user.getSchedules())
                .email("")
                .phoneNumber("")
                .status(user.getStatus())
                .imageUrl(imageStorageService.getImageUrl(user.getImageKey()))
                .build();
    }

}
