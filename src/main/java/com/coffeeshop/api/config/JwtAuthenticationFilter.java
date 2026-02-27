
package com.coffeeshop.api.config;

import com.coffeeshop.api.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;


@Component
@RequiredArgsConstructor
public final class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/auth/login",
            "/auth/refresh",
            "/auth/register",
            "/actuator/health",
            "/public",
            "/payway/callback"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1) Skip CORS preflight
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2) Skip public endpoints
        final String path = request.getRequestURI();
        if (isPublic(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3) Avoid overriding an existing authenticated context
        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        if (existing != null && existing.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4) Extract Bearer token
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7).trim();
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 5) Parse username from token
            String username = jwtService.extractUsername(token);
            if (username == null || username.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            // 6) Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!isUserUsable(userDetails)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // 7) Validate token against user (if your JwtService supports it)
            if (!isTokenValidAgainstUser(token, userDetails)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // 8) Create Authentication and set context
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            // Defensive: clear context and continue anonymously
            SecurityContextHolder.clearContext();
            // Optional logging:
            // log.warn("JWT authentication error", e);
        }

        // 9) Continue chain
        filterChain.doFilter(request, response);
    }

    private boolean isPublic(String path) {
        if (path == null) return true;
        for (String root : PUBLIC_PATHS) {
            if (path.equals(root) || path.startsWith(root + "/")) {
                return true;
            }
        }
        return false;
    }

    private boolean isUserUsable(UserDetails userDetails) {
        return userDetails != null
                && userDetails.isEnabled()
                && userDetails.isAccountNonExpired()
                && userDetails.isAccountNonLocked()
                && userDetails.isCredentialsNonExpired();
    }






    private boolean isTokenValidAgainstUser(String token, UserDetails userDetails) {
        try {
            // Preferred API if you have it
            // (signature, expiry, issuer, audience, etc.)
            return jwtService.isTokenValid(token, userDetails);
        } catch (NoSuchMethodError | UnsupportedOperationException ignored) {
            // Fallback: basic subject match (not as strong as signature/expiry validation)
            String subject = null;
            try {
                subject = jwtService.extractUsername(token);
            } catch (Exception e) {
                return false;
            }
            return subject != null && subject.equalsIgnoreCase(userDetails.getUsername());
        }
    }
}

