package com.coffeeshop.api.serviceimpl;

import com.coffeeshop.api.config.JwtService;
import com.coffeeshop.api.domain.RefreshToken;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.dto.AccessTokenResponse;
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
    public AccessTokenResponse generateAccessTokenFromRefreshToken(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Refresh token must not be blank");
        }


        // Lookup refresh token in DB
        RefreshToken rfToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Invalid refresh token"));

        // Check if token is revoked or expired
        if (rfToken.isRevoked() || rfToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Refresh token is expired or revoked");
        }

        // Get user
        User user = rfToken.getUser();
        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Account disabled");
        }

//        refreshTokenRepository.deleteByUser_Id(user.getId());
//        refreshTokenRepository.flush();

        refreshTokenRepository.delete(rfToken);  // Delete only this specific token

        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);


        RefreshToken newRf = RefreshToken.builder()
                .token(newRefreshToken)
                .user(user)
                .expiresAt(jwtService.getRefreshExpiryInstant())
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRf);

        log.debug("Rotated refresh token for userId={}", user.getId());

        // Return response with refresh info (existing refresh token + expiry)
        return new AccessTokenResponse(
                newAccessToken,
                "Bearer",
                jwtService.getExpiresInSeconds(),
                new AccessTokenResponse.Refresh(
                        newRefreshToken,
                        newRf.getExpiresAt()
                )
        );
    }
}
