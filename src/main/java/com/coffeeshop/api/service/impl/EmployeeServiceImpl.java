package com.coffeeshop.api.service.impl;

import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.Role;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
                        .page(pageable.getPageNumber() + 1)
                        .size(pageable.getPageSize())
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
    public GetAllEmployeeProfilesResponse.Employee editEmployeeDetail(UUID id, EditStaffRequest request, MultipartFile image) {
        authorizationGuard.requireAdmin();

        User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found"
        ));

        boolean noFields = request == null || isAllBlank(request);
        if (noFields && (image == null || image.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one field must be provided.");
        }

        if (request != null) {
            applyName(user, request.name());
            applyUsername(user, request.username());
            applyPassword(user, request.password());
            if (request.role() != null) user.setRole(request.role());
            if (request.status() != null) user.setStatus(request.status());
            if (request.shiftType() != null) user.setShiftType(request.shiftType());
            if (request.schedules() != null && !request.schedules().isEmpty()) user.setSchedules(request.schedules());
        }

        applyImage(user, image);
        user.setUpdatedAt(ZonedDateTime.now(BUSINESS_TZ).toInstant());

        User savedUser = userRepository.save(user);
        GetAllEmployeeProfilesResponse.Employee response = userMapper.toEmployeeResponseDto(savedUser);

        // WebSocket
        webSocketEventPublisher.publishEmployeeUpdateToAllAdmins(response);

        return response;
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
    private void applyImage(User user, MultipartFile image) {
        if (image == null || image.isEmpty()) return;
        String oldKey = user.getImageKey();
        String newKey = uploadEmployeeImage(image);
        user.setImageKey(newKey);
        if (oldKey != null) {
            try { imageStorageService.delete(oldKey); }
            catch (Exception e) { /* log warning */ }
        }
    }

    // BLANK CHECK
    private boolean isAllBlank(EditStaffRequest r) {
        return r.name() == null && r.username() == null && r.password() == null
                && r.role() == null && r.status() == null && r.shiftType() == null
                && (r.schedules() == null || r.schedules().isEmpty());
    }

    // NAME
    private void applyName(User user, String name) {
        if (name != null && !name.isBlank()) user.setName(name.trim());
    }

    // USERNAME
    private void applyUsername(User user, String username) {
        if (username == null || username.isBlank()) return;
        String newU = username.trim().toLowerCase();
        if (!newU.equals(user.getUsername()) && userRepository.existsByUsername(newU)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists.");
        }
        user.setUsername(newU);
    }

    // PASSWORD
    private void applyPassword(User user, String password) {
        if (password == null || password.isBlank()) return;
        if (!password.trim().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).{8,}$")) {
            throw badRequest("Weak password");
        }
        user.setPassword(passwordEncoder.encode(password.trim()));
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
