package com.coffeeshop.api.service.impl;

import com.coffeeshop.api.config.JwtService;
import com.coffeeshop.api.domain.RefreshToken;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.dto.*;
import com.coffeeshop.api.dto.auth.*;
import com.coffeeshop.api.minio.ImageStorageService;
import com.coffeeshop.api.repository.RefreshTokenRepository;
import com.coffeeshop.api.repository.UserRepository;
import com.coffeeshop.api.security.AuthorizationGuard;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {


    private final AuthorizationGuard authorizationGuard;
    private final ImageStorageService imageStorageService;

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






    @Override
    public GetUserProfile getProfile() {
        User user = authorizationGuard.requireAnyRoles(Role.ADMIN, Role.CASHIER, Role.BARISTA);

        return GetUserProfile.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .imageUrl(imageStorageService.getImageUrl(user.getImageKey()))
                .role(user.getRole())
                .build();
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
}
