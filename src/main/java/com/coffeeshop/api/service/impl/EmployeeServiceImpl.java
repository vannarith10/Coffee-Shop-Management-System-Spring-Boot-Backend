package com.coffeeshop.api.service.impl;

import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.domain.enums.Schedule;
import com.coffeeshop.api.domain.enums.ShiftType;
import com.coffeeshop.api.domain.enums.Status;
import com.coffeeshop.api.dto.Pagination;
import com.coffeeshop.api.dto.adminDashboard.staff.AddNewEmployeeRequest;
import com.coffeeshop.api.dto.adminDashboard.staff.EditStaffRequest;
import com.coffeeshop.api.dto.adminDashboard.staff.GetAllEmployeeProfilesResponse;
import com.coffeeshop.api.helper.PaginationHelper;
import com.coffeeshop.api.mapper.UserMapper;
import com.coffeeshop.api.minio.ImageStorageService;
import com.coffeeshop.api.repository.UserRepository;
import com.coffeeshop.api.security.AuthorizationGuard;
import com.coffeeshop.api.service.EmployeeService;
import com.coffeeshop.api.websocket.WebSocketEventPublisher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {


    private final AuthorizationGuard authorizationGuard;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ImageStorageService imageStorageService;
    private final PasswordEncoder passwordEncoder;
    private final WebSocketEventPublisher webSocketEventPublisher;
    private static final ZoneId BUSINESS_TZ = ZoneId.of("Asia/Phnom_Penh");



    //=======================
    // Get All Employees
    //=======================
    @Override
    public GetAllEmployeeProfilesResponse getAllEmployeeProfiles(int page, int size) {
        authorizationGuard.requireAdmin();

        Pageable pageable = PaginationHelper.of(page, size);
        Page<User> userPage = userRepository.findAllByRolePriority(pageable);

        List<GetAllEmployeeProfilesResponse.Employee> staffList = userPage
                .getContent()
                .stream()
                .map(userMapper::toEmployeeResponseDto)
                .toList();

        var pagination = Pagination.builder()
                        .page(userPage.getNumber() + 1)
                        .size(userPage.getSize())
                        .itemCount(userPage.getNumberOfElements())
                        .totalPages(userPage.getTotalPages())
                        .totalItems(userPage.getTotalElements())
                        .build();

        return GetAllEmployeeProfilesResponse.builder()
                .message("Get all employee profiles information")
                .pagination(pagination)
                .staffs(staffList)
                .build();
    }




    // ================================
    // ADD NEW EMPLOYEE
    // ================================
    @Transactional
    @Override
    public GetAllEmployeeProfilesResponse.Employee addNewEmployee(AddNewEmployeeRequest request, MultipartFile image) {
        authorizationGuard.requireAdmin();

        validateNewEmployee(request);

        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
        }

        String imageKey = uploadEmployeeImage(image);

        User user = User.builder()
                .name(request.fullName().trim())
                .username(request.username().trim().toLowerCase())
                .password(passwordEncoder.encode(request.password().trim()))
                .role(request.role())
                .isActive(true)
                .status(request.status())
                .createdAt(ZonedDateTime.now(BUSINESS_TZ).toInstant())
                .shiftType(request.shift())
                .schedules(request.schedules())
                .imageKey(imageKey)
                .build();

        var response = userMapper.toEmployeeResponseDto(userRepository.save(user));
        webSocketEventPublisher.publishCreateStaffToAdmins(response);

        return response;
    }



    //----------------------------
    // EDIT EMPLOYEE DETAIL
    //----------------------------
    @Transactional
    @Override
    public void editEmployeeDetail(
            UUID id,
            EditStaffRequest request,
            MultipartFile image
    ) {
        authorizationGuard.requireAdmin();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        boolean noFields = request == null || isAllBlank(request);

        if (noFields && (image == null || image.isEmpty())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one field must be provided."
            );
        }

        boolean changed = false;

        if (request != null) {
            changed |= applyName(user, request.name());
            changed |= applyUsername(user, request.username());
            changed |= applyPassword(user, request.password());
            changed |= applyEmail(user, request.email());
            changed |= applyRole(user, request.role());
            changed |= applyStatus(user, request.status());
            changed |= applyShiftType(user, request.shiftType());
            changed |= applySchedule(user, request.schedules());
        }

        changed |= applyImage(user, image);

        if (!changed) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No changes detected"
            );
        }

        user.setUpdatedAt(
                ZonedDateTime.now(BUSINESS_TZ).toInstant()
        );

        User savedUser = userRepository.save(user);

        // build response
        GetAllEmployeeProfilesResponse.Employee response =
                userMapper.toEmployeeResponseDto(savedUser);

        webSocketEventPublisher.publishEmployeeUpdateToAllAdmins(response);
    }



    //===================================
    //
    // HELPER
    //
    //===================================


    // ADD NEW EMPLOYEE VALIDATION
    private void validateNewEmployee(AddNewEmployeeRequest r) {
        if (r.fullName() == null || r.fullName().isBlank()) throw badRequest("Full name required");
        if (r.username() == null || r.username().isBlank()) throw badRequest("Username required");
        if (r.password() == null || r.password().isBlank()) throw badRequest("Password required");
        if (r.role() == null) throw badRequest("Role required");
        if (r.shift() == null) throw badRequest("Shift required");
        if (r.schedules() == null || r.schedules().isEmpty()) throw badRequest("Working days required");
        if (r.status() == null) throw badRequest("Status required");
        if (!r.password().trim().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).{8,}$")) {
            throw badRequest("Weak password");
        }
    }

    // BAD REQUEST RESPONSE STATUS
    private ResponseStatusException badRequest(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    // UPLOAD EMPLOYEE IMAGE
    private String uploadEmployeeImage(MultipartFile image) {
        if (image == null || image.isEmpty()) return null;
        imageStorageService.ensureBucketExists();
        try {
            return imageStorageService.upload(image, imageStorageService.employeeFolder());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image.", ex);
        }
    }

    // UPLOAD NEW IMAGE AND DELETE OLD
    private boolean applyImage (User user, MultipartFile image) {
        if (image == null || image.isEmpty()) return false;
        String oldKey = user.getImageKey();
        String newKey = uploadEmployeeImage(image);
        user.setImageKey(newKey);
        if (oldKey != null) {
            try { imageStorageService.delete(oldKey); }
            catch (Exception e) { /* log warning */ }
        }
        return true;
    }

    // BLANK CHECK
    private boolean isAllBlank (EditStaffRequest r) {
        return r.name() == null
                && r.username() == null
                && r.password() == null
                && r.email() == null
                && r.role() == null
                && r.status() == null
                && r.shiftType() == null
                && (r.schedules() == null || r.schedules().isEmpty());
    }

    // NAME
    private boolean applyName(User user, String name) {
        if (name == null || name.isBlank()) {
            return false;
        }

        String newName = name.trim();

        if (Objects.equals(user.getName(), newName)) {
            return false;
        }

        user.setName(newName);
        return true;
    }

    // USERNAME
    private boolean applyUsername(User user, String username) {
        if (username == null || username.isBlank()) {
            return false;
        }

        String newUsername = username.trim().toLowerCase();

        if (Objects.equals(user.getUsername(), newUsername)) {
            return false;
        }

        if (userRepository.existsByUsername(newUsername)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists."
            );
        }

        user.setUsername(newUsername);
        return true;
    }


    // PASSWORD
    private boolean applyPassword(User user, String password) {
        if (password == null || password.isBlank()) {
            return false;
        }

        String newPassword = password.trim();

        if (!newPassword.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).{8,}$")) {
            throw badRequest("Weak password");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return true;
    }

    // Email
    private boolean applyEmail(User user, String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String newEmail = email.trim().toLowerCase();

        if (Objects.equals(user.getEmail(), newEmail)) {
            return false;
        }

        if (userRepository.existsByEmail(newEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists."
            );
        }

        user.setEmail(newEmail);
        return true;
    }

    // Role
    private boolean applyRole(User user, Role role) {
        if (role == null) {
            return false;
        }

        if (Objects.equals(user.getRole(), role)) {
            return false;
        }

        user.setRole(role);
        return true;
    }

    // Status
    private boolean applyStatus(User user, Status status) {
        if (status == null) {
            return false;
        }

        if (Objects.equals(user.getStatus(), status)) {
            return false;
        }

        user.setStatus(status);
        return true;
    }

    // Shift
    private boolean applyShiftType(User user, ShiftType shiftType) {
        if (shiftType == null) {
            return false;
        }

        if (Objects.equals(user.getShiftType(), shiftType)) {
            return false;
        }

        user.setShiftType(shiftType);
        return true;
    }

    // Schedule
    private boolean applySchedule(User user, List<Schedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return false;
        }

        if (new HashSet<>(user.getSchedules())
                .equals(new HashSet<>(schedules))) {
            return false;
        }

        user.setSchedules(schedules);
        return true;
    }




    // ==============================
    // Delete Profile
    // ==============================
    @Transactional
    @Override
    public void deleteProfile(UUID id) {
        authorizationGuard.requireAdmin();

        User user = userRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );

        String key = user.getImageKey();

        userRepository.delete(user);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        imageStorageService.delete(key);
                    }
                }
        );
    }
}
