package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.adminDashboard.staff.AddNewEmployeeRequest;
import com.coffeeshop.api.dto.adminDashboard.staff.EditStaffRequest;
import com.coffeeshop.api.dto.adminDashboard.staff.GetAllEmployeeProfilesResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface EmployeeService {

    GetAllEmployeeProfilesResponse getAllEmployeeProfiles (int page, int size);

    GetAllEmployeeProfilesResponse.Employee addNewEmployee (AddNewEmployeeRequest request, MultipartFile image);

    GetAllEmployeeProfilesResponse.Employee editEmployeeDetail (UUID id, EditStaffRequest request, MultipartFile image);

}
