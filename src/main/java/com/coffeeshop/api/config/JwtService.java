package com.coffeeshop.api.config;

import com.coffeeshop.api.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.expiration}")
    private long expiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    // Optional: allow small clock skew (e.g., 60s)
    private static final long CLOCK_SKEW_MILLIS = 60_000L;



    private Key getSigningKey() {
        // HS256 requires at least a 256-bit key; ensure your secret length is sufficient.
        // Use a long random string (32+ bytes). Consider env var or Secrets Manager.
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    //==================== GENERATE ACCESS TOKEN ====================//
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(user.getId().toString())       // subject = user id
                .claim("username", user.getUsername())      // convenience claim
                .claim("role", user.getRole().name())       // role claim (e.g. ADMIN/STAFF)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }



    //==================== GENERATE REFRESH TOKEN ====================//
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }





    // ==================== VALIDATE TOKEN ==================== //
    public boolean isTokenValid(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
        try {
            Claims claims = extractClaims(token);
            if (claims.getSubject() == null || claims.getSubject().isBlank()) {
                return false;
            }
            Date exp = claims.getExpiration();
            if (exp == null) {
                return false;
            }
            long now = System.currentTimeMillis();
            if (exp.getTime() < (now - CLOCK_SKEW_MILLIS)) {
                return false;
            }
            // Ensure username claim matches the loaded user's username
            String usernameClaim = claims.get("username", String.class);
            return usernameClaim != null && usernameClaim.equalsIgnoreCase(userDetails.getUsername());
        } catch (Exception e) {
            return false;
        }
    }





    public Claims extractClaims(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
        return jws.getBody();
    }




    // ==================== GET USER ID ==================== //
    public UUID extractUserId(String token) {
        String subject = extractClaims(token).getSubject();

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("JWT subject (userId) is missing");
        }
        return UUID.fromString(subject);
    }




    // =============== GET REFRESH TOKEN EXPIRES TIME =============== //
    public Instant getRefreshExpiryInstant() {
        return Instant.now().plusMillis(refreshExpiration);
    }



    // =============== GET ACCESS TOKEN EXPIRE TIME ================ //
    public long getExpiresInSeconds() {
        return expiration / 1000L;
    }



    // =============== GET USERNAME from TOKEN =============== //
    public String extractUsername(String token) {
        return extractClaims(token).get("username", String.class);
    }


}
