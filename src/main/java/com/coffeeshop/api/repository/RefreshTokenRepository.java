package com.coffeeshop.api.repository;

import com.coffeeshop.api.domain.RefreshToken;
import com.coffeeshop.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser_Id(UUID userId);

    List<RefreshToken> findAllByUserAndRevokedFalse(User user);

    int deleteByRevokedTrueAndExpiresAtBefore(Instant cutoff);


    @Modifying
    @Query("""
        DELETE FROM RefreshToken rt   
        WHERE rt.revoked = true 
        AND rt.expiresAt < CURRENT_TIMESTAMP
    """)
    int deleteExpiredRevokedTokens ();

}
