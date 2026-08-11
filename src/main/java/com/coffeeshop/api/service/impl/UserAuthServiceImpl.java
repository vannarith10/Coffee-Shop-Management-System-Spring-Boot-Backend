package com.coffeeshop.api.service.impl;

import ch.qos.logback.core.util.StringUtil;
import com.coffeeshop.api.config.JwtService;
import com.coffeeshop.api.domain.RefreshToken;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.Status;
import com.coffeeshop.api.dto.auth.LoginRequest;
import com.coffeeshop.api.dto.auth.LoginResponse;
import com.coffeeshop.api.minio.ImageStorageService;
import com.coffeeshop.api.repository.RefreshTokenRepository;
import com.coffeeshop.api.repository.UserRepository;
import com.coffeeshop.api.security.CustomUserDetails;
import com.coffeeshop.api.service.UserAuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthServiceImpl implements UserAuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final ImageStorageService imageStorageService;


    //---------------
    // LOGIN
    //---------------
    @Transactional
    @Override
    public LoginResponse login(LoginRequest request) {
        if (request == null || !StringUtils.hasText(request.username())
            || !StringUtils.hasText(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Username and Password required.");
        }

        String normalized = request.username().trim().toLowerCase();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    normalized, request.password()));
            User user = userRepository.findByUsernameIgnoreCase(normalized)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "Invalid credentials."));
            if (!user.isActive()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Account disabled.");
            }
            if (user.getStatus() == Status.SUSPENDED || user.getStatus() == Status.INACTIVE) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account disabled.");
            }

            refreshTokenRepository.deleteByUser_Id(user.getId());
            refreshTokenRepository.flush();

            String accessToken = jwtService.generateAccessToken(user);
            long accessExpiry = jwtService.getExpiresInSeconds();
            String refreshToken = jwtService.generateRefreshToken(user);
            Instant refreshExpiry = jwtService.getRefreshExpiryInstant();

            // BUILD REFRESH TOKEN
            RefreshToken rToken = RefreshToken.builder()
                    .token(refreshToken)
                    .user(user)
                    .expiresAt(refreshExpiry)
                    .revoked(false)
                    .build();

            refreshTokenRepository.save(rToken);

            // BUILD RESPONSE
            LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .role(user.getRole())
                    .imageUrl(imageStorageService.getImageUrl(user.getImageKey()))
                    .build();

            LoginResponse.Refresh refresh = LoginResponse.Refresh.builder()
                    .token(refreshToken)
                    .expiresAt(refreshExpiry)
                    .build();

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .expiresIn(accessExpiry)
                    .refresh(refresh)
                    .userInfo(userInfo)
                    .build();

        } catch (DisabledException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account disabled");
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Login failed");
        }
    }



    //--------------
    // GET USER ID
    //--------------
    @Override
    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Not authenticated");
        }

        if (auth.getPrincipal() instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getId();
        }

        throw new IllegalStateException("Unexpected principal");
    }



    //----------------
    // GET USERNAME
    //----------------
    @Override
    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Not authenticated");
        }

        return auth.getName();
    }

}


