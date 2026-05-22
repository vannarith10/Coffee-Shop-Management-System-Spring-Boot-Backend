package com.coffeeshop.api.controller;


import com.coffeeshop.api.dto.adminDashboard.staff.AddNewEmployeeRequest;
import com.coffeeshop.api.dto.adminDashboard.staff.EditStaffRequest;
import com.coffeeshop.api.dto.adminDashboard.staff.GetAllEmployeeProfilesResponse;
import com.coffeeshop.api.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/employee")
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeController {

    private final EmployeeService employeeService;



    // GET ALL EMPLOYEE PROFILES
    @GetMapping("/profiles")
    public ResponseEntity<GetAllEmployeeProfilesResponse> getEmployeeProfiles (
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(employeeService.getAllEmployeeProfiles(page, size));
    }



    // CREATE EMPLOYEE PROFILE
    @PostMapping(value = "/create-account", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GetAllEmployeeProfilesResponse.Employee> addNewEmployee (
            @RequestPart("data")AddNewEmployeeRequest request,
            @RequestPart(value = "image", required = false)MultipartFile image
            ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.addNewEmployee(request, image));
    }



    // PATCH EMPLOYEE
    @PatchMapping(value = "/{id}/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GetAllEmployeeProfilesResponse.Employee> patchEmployee (
            @PathVariable UUID id,
            @RequestPart(required = false) @Valid EditStaffRequest request,
            @RequestPart(required = false) MultipartFile image
            ) {
        return ResponseEntity.ok(employeeService.editEmployeeDetail(id, request, image));
    }
}
