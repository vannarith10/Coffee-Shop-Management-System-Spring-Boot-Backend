package com.coffeeshop.api.config;


import com.coffeeshop.api.exception.ExceptionResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {


    private final JwtAuthFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;
    private final CorsConfigurationSource corsConfigurationSource;


    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/test/**").permitAll()

                        // Auth
                        .requestMatchers(HttpMethod.POST, "/api/v2/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/token/get-access-token").permitAll()


                        // Public Menu & Shop Info
                        .requestMatchers(HttpMethod.GET, "/api/v2/shop-profile/shop-name/shop-image").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/product/user-menu").permitAll()


                        // Websocker
                        .requestMatchers("/ws/**").permitAll()


                        // Everything else needs auth
                        // @PreAuthorize handles roles on controllers

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }



    private void writeJson(HttpServletResponse response, HttpStatus status, Object body) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }



    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            var error = ExceptionResponse.<String>builder()
                    .message("Authentication Error")
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .timestamp(LocalDateTime.now())
                    .detail(authException.getMessage())
                    .build();
            writeJson(response, HttpStatus.UNAUTHORIZED, error);
        };
    }



    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            var error = ExceptionResponse.<String>builder()
                    .message("Authorization Error")
                    .status(HttpStatus.FORBIDDEN.value())
                    .timestamp(LocalDateTime.now())
                    .detail(accessDeniedException.getMessage())
                    .build();
            writeJson(response, HttpStatus.FORBIDDEN, error);
        };
    }



    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
