package com.coffeeshop.api.helper;

import com.coffeeshop.api.domain.RefreshToken;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenHelper {


    private final RefreshTokenRepository refreshTokenRepository;



    @Transactional
    public void revokeAllUserTokens(User user) {
        List<RefreshToken> activeTokens =
                refreshTokenRepository.findAllByUserAndRevokedFalse(user);

        if (activeTokens.isEmpty()) {
            return;
        }

        for (RefreshToken token : activeTokens) {
            token.setRevoked(true);
        }

        refreshTokenRepository.saveAll(activeTokens);

        log.warn("Revoked {} active refresh tokens for user: {}",
                activeTokens.size(), user.getUsername());
    }



    @Transactional
    public void revokeToken(RefreshToken token) {
        if (!token.isRevoked()) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);

            log.info("Revoked refresh token for user: {}",
                    token.getUser().getUsername());
        }
    }



    @Scheduled(cron = "0 0 2 * * ?") // every day at 2 AM
    @Transactional
    public void cleanOldTokens() {
        Instant cutoff = Instant.now().minus(30, ChronoUnit.DAYS);

        int deleted = refreshTokenRepository
                .deleteByRevokedTrueAndExpiresAtBefore(cutoff);

        log.info("Cleaned up {} old refresh tokens", deleted);
    }


}
