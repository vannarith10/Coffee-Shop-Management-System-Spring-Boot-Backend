package com.coffeeshop.api.serviceimpl;

import com.coffeeshop.api.config.JwtService;
import com.coffeeshop.api.domain.RefreshToken;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.dto.*;
import com.coffeeshop.api.dto.auth.*;
import com.coffeeshop.api.repository.RefreshTokenRepository;
import com.coffeeshop.api.repository.UserRepository;
import com.coffeeshop.api.security.CustomUserDetails;
import com.coffeeshop.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;



    // ==================== CREATE ACCOUNT ==================== //
    // ===== Admin Only ===== //
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    @Transactional
    public RegisterResponse createAccount(RegisterRequest request) {

        if (request == null || !StringUtils.hasText(request.username()) || !StringUtils.hasText(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password must not be blank");
        }

        final String normalizedUsername = request.username().trim().toLowerCase();

        if (request.password().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        }

        if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }


        // ===== Strong Password Required ===== //
        Pattern strongPolicy = Pattern.compile(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$"
        );
        if (!strongPolicy.matcher(request.password()).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must have upper & lower case letters, a digit, and a special character."
            );
        }


        // ========== Validate Role Input ========== //
        String rawRole = request.role();
        if (rawRole == null || rawRole.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required.");
        }

        Role role;
        try {
            role = Role.valueOf(rawRole.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid role...!"
            );
        }

        // ========== Create User ========== //
        User user = User.builder()
                .username(normalizedUsername)
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .isActive(true)
                .createdAt(Instant.now())
                .build();

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {

            String msg = Optional.of(ex.getMostSpecificCause())
                    .map(Throwable::getMessage)
                    .orElse(ex.getMessage());

            // Helpful logging
            log.error("Create account failed for '{}': {}", normalizedUsername, msg, ex);

            // Map well-known unique constraints
            if (msg != null && msg.contains("uq_user_username")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
            }

            // Fallback to BAD_REQUEST with the actual DB reason
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Integrity violation: " + msg);

        }

        return new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }





    // ========================= LOGIN ========================= //
    @Transactional
    @Override
    public LoginResponse login(LoginRequest request) {
        if (request == null || !StringUtils.hasText(request.username()) || !StringUtils.hasText(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password must not be blank");
        }

        final String normalizedUsername = request.username().trim().toLowerCase();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedUsername, request.password())
            );

            User user = userRepository.findByUsernameIgnoreCase(normalizedUsername)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

            if (!user.isActive()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled");
            }

            // Remove old refresh tokens
            refreshTokenRepository.deleteByUser_Id(user.getId());
            refreshTokenRepository.flush();

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            long expiresInSeconds = jwtService.getExpiresInSeconds();
            Instant expiresRefresh = jwtService.getRefreshExpiryInstant();

            refreshTokenRepository.save(
                    RefreshToken.builder()
                            .token(refreshToken)
                            .user(user)
                            .expiresAt(expiresRefresh)
                            .revoked(false)
                            .build()
            );

            return new LoginResponse(
                    accessToken,
                    "Bearer",
                    expiresInSeconds,
                    new LoginResponse.Refresh(
                            refreshToken,
                            expiresRefresh
                    ),
                    new LoginResponse.UserInfo(
                            user.getId(),
                            user.getUsername(),
                            user.getRole()
                    )
            );

        } catch (DisabledException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled");
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        } catch (ResponseStatusException ex) {
            throw ex; // propagate our own specific errors
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to process login");
        }
    }






    // =============== SET NEW PASSWORD =============== //
    // ===== Admin Only ===== //
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Override
    public void setNewPassword(NewPasswordRequest request) {


        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required.");
        }
        if(request.newPassword() == null || request.newPassword().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must not be null.");
        }
        if(request.confirmPassword() == null || request.confirmPassword().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must not be null.");
        }
        if(!request.newPassword().equals(request.confirmPassword())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confirm password does not match.");
        }
        if(request.newPassword().length() < 8){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters.");
        }
        if(request.username().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot be null.");
        }


        // ===== Strong Password ===== //
        Pattern strongPolicy = Pattern.compile(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,}$"
        );
        if (!strongPolicy.matcher(request.newPassword()).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must have upper & lower case letters, a digit, and a special character."
            );
        }

        // ===== Get User ===== //
        User user = userRepository.findByUsernameIgnoreCase(request.username().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));


        // ===== Prevent Reusing the Old Password ===== //
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must different from the current password.");
        }

        // ===== Update and Save ===== //
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }






    // ==================== DISABLE USER ==================== //
    // ===== Admin Only ===== //
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Override
    //
    // User token will automatically be disabled too by JwtAuthenticationFilter class
    //
    public void disableUser(DisableUserRequest request) {
        if(request == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        if(request.username() == null || request.username().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }
        if(request.username().equals(getCurrentUsername())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot disable yourself.");
        }

        String username = request.username().trim();
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        if(!user.isActive()){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already disabled.");
        }

        user.setActive(false);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }





    // ==================== ENABLE USER ==================== //
    // ===== Admin Only ===== //
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Override
    public void enableUser(EnableUserRequest request) {
        if(request == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is required.");
        }
        if(request.username() == null || request.username().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required.");
        }
        if(request.username().equals(getCurrentUsername())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot enable yourself.");
        }

        String username = request.username().trim();
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if(user.isActive()){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already enabled");
        }

        user.setActive(true);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }





    // ==================== GET ALL DISABLED USERS ==================== //
    // ===== Admin Only ===== //
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Override
    public List<DisableUserResponse> getAllDisabledUsers() {
        List<User> users = userRepository.findAllByIsActiveFalse();
        return users.stream()
                .map(this::toDisableUserResponse)
                .toList();
    }






    // ==================== GET CURRENT USER ID ==================== //
    @Override
    public UUID getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // 1) Ensure request is authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        Object principal = authentication.getPrincipal();

        // 2) Extract userId safely
        if (principal instanceof CustomUserDetails customUser) {
            return customUser.getId(); // UUID
        }
        // 3) Fallback (should not normally happen)
        throw new IllegalStateException("Unexpected authentication principal");
    }





    // ==================== GET CURRENT USERNAME ==================== //
    @Override
    public String getCurrentUsername() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        return authentication.getName(); // comes from UserDetails.getUsername()
    }




    // ========================= MAPPER ========================= //
    private DisableUserResponse toDisableUserResponse(User user) {
        return new DisableUserResponse(
                user.getUsername(),
                user.getId(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }




    //////////////////////////////////////////////////////////////////////////////////////////////
}
