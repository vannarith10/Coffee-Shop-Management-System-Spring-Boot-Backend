package com.coffeeshop.api.service.impl;

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


    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;



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



    //////////////////////////////////////////////////////////////////////////////////////////////
}
