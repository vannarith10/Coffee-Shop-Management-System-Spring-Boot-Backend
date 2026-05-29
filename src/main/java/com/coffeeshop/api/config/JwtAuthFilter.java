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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public final class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserDetailsService userDetailsService;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }


        final String path = request.getRequestURI();
        if (isPublic(path)) {
            filterChain.doFilter(request, response);
            return;
        }


        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        if (existing != null && existing.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }


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


        try{
            String username = jwtService.extractUsername(token);
            if (username == null || username.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!isUserUsable(userDetails)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }


            if (!isTokenValidAgainstUser(token, userDetails)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }


            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }




    private boolean isPublic (String path) {
        if (path == null) return true;

        return path.startsWith("/api/v2/product/user-menu")
                || path.startsWith("/api/v2/shop-profile/shop-name/shop-image")
                || path.startsWith("/api/v1/auth/")
                || path.startsWith("/api/v2/auth/")
                || path.startsWith("/ws/");
    }




    private boolean isUserUsable (UserDetails userDetails) {
        return userDetails != null
                && userDetails.isEnabled()
                && userDetails.isAccountNonExpired()
                && userDetails.isAccountNonLocked()
                && userDetails.isCredentialsNonExpired();
    }




    private boolean isTokenValidAgainstUser(String token, UserDetails userDetails) {
        try {
            return jwtService.isTokenValid(token, userDetails);
        } catch (NoSuchMethodError | UnsupportedOperationException ignored) {
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
