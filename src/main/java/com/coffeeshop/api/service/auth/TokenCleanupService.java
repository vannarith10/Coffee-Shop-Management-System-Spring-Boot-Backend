package com.coffeeshop.api.service.auth;

import com.coffeeshop.api.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    RefreshTokenRepository refreshTokenRepository;


    /**
     * Automatically executed by Spring Scheduler.
     * Must remain public for Spring AOP proxying to work.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanUpExpiredRevokedTokens () {
        int deletedCount = refreshTokenRepository.deleteExpiredRevokedTokens();

        if (deletedCount > 0) {
            log.info("Cleaned up {} expired, revoked refresh tokens", deletedCount);
        }
    }

}
