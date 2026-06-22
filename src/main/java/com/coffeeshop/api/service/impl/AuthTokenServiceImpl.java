package com.coffeeshop.api.service.impl;

import com.coffeeshop.api.config.JwtService;
import com.coffeeshop.api.domain.RefreshToken;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.Status;
import com.coffeeshop.api.dto.auth.LoginResponse;
import com.coffeeshop.api.dto.auth.RefreshAccessTokenResponse;
import com.coffeeshop.api.helper.RefreshTokenHelper;
import com.coffeeshop.api.repository.RefreshTokenRepository;
import com.coffeeshop.api.service.AuthTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthTokenServiceImpl implements AuthTokenService {


    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;



    @Transactional
    @Override
    public RefreshAccessTokenResponse generateAccessTokenFromRefreshToken(String refreshToken) {

        // Validate Input
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Refresh token must not be blank");
        }


        // Is this user's RFToken valid ?? Let's find
        // Lookup refresh token in DB | Checking
        RefreshToken existingToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Invalid refresh token"));

        // Revoke check
        if (existingToken.isRevoked()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Refresh token reuse detected");
        }

        // Expiry check
        if (!existingToken.getExpiresAt().isAfter(Instant.now())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }


        // Get user
        User user = existingToken.getUser();

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Account disabled");
        }

        // Status check
        if (user.getStatus() == Status.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has been suspended");
        }



        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        long accessExpiry = jwtService.getExpiresInSeconds();
        Instant refreshExpiry = jwtService.getRefreshExpiryInstant();


        existingToken.setToken(newRefreshToken);
        existingToken.setExpiresAt(refreshExpiry);
        existingToken.setRevoked(false);

        // Save new
        refreshTokenRepository.save(existingToken);



        RefreshAccessTokenResponse.Refresh refresh =
                RefreshAccessTokenResponse.Refresh.builder()
                        .token(newRefreshToken)
                        .expiresAt(refreshExpiry)
                        .build();

        RefreshAccessTokenResponse.UserInfo userInfo =
                RefreshAccessTokenResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .role(user.getRole())
                        .build();

        // Return response with refresh info
        return RefreshAccessTokenResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(accessExpiry)
                .refresh(refresh)
                .userInfo(userInfo)
                .build();
    }





    //=================================
    // GENERATE TOKEN
    //=================================
    @Transactional
    @Override
    public LoginResponse generateTokenResponse(User user) {
        refreshTokenRepository.deleteByUser_Id(user.getId());
        refreshTokenRepository.flush();

        String accessToken = jwtService.generateAccessToken(user);
        long accessExpiry = jwtService.getExpiresInSeconds();
        String refreshToken = jwtService.generateRefreshToken(user);
        Instant refreshExpiry = jwtService.getRefreshExpiryInstant();

        RefreshToken rToken = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiresAt(refreshExpiry)
                .revoked(false)
                .build();
        refreshTokenRepository.save(rToken);

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
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
    }


}
