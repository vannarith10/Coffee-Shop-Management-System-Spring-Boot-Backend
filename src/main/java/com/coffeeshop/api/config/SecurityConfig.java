package com.coffeeshop.api.config;

import com.coffeeshop.api.domain.enums.Role;
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
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final ObjectMapper objectMapper;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(ex -> ex
                        // ====================
                        // HANDLES UNAUTHORIZED
                        // ====================
                        .authenticationEntryPoint(authenticationEntryPoint())

                        // ====================
                        // HANDLES FORBIDDEN
                        // ====================
                        .accessDeniedHandler(accessDeniedHandler())
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ====================
                        // TEST
                        // ====================
                        // 🔥 Allow React preflight CORS requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/test/**").permitAll()

                        // 🔥 Allow your PAYMENT endpoints (NO AUTH)
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/create").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/callback").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/payments/generate-qr").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/payments/check-transaction/{tranId}").permitAll()

                        // ✅ Allow ABA callback endpoint without Bearer token
                        .requestMatchers(HttpMethod.POST, "/payway/callback").permitAll()


                        // Swagger
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // ========================================================================================== //
                        //
                        //
                        //
                        //
                        //
                        //
                        //
                        // ====================
                        // AUTH
                        // ====================
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()


                        // ====================
                        // TOKEN
                        // ====================
                        .requestMatchers(HttpMethod.POST, "/api/v1/token/get-acceess-token").permitAll()


                        // ====================
                        // ADMIN
                        // ====================
                        .requestMatchers(HttpMethod.POST,"/api/v1/auth/register").hasRole(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.POST, "/api/v1/user/change-password").hasRole(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.PUT, "/api/v1/user/disable").hasRole(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.PUT, "/api/v1/user/enable").hasRole(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.GET, "/api/v1/user/get-all-disabled-users").hasRole(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.POST, "/api/v1/discounts").hasRole(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.POST, "/api/v1/product").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/category").hasRole(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.POST, "/api/v1/product/update").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/product/update-price").hasRole(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/product/*").hasRole(Role.ADMIN.toString())
                        .requestMatchers(HttpMethod.PUT, "/api/v1/product/*/image").hasRole(Role.ADMIN.toString())


                        // ====================
                        // CASHIER
                        // ====================
                        .requestMatchers(HttpMethod.POST, "/api/v1/order/create-order").hasRole(Role.CASHIER.toString())
                        .requestMatchers(HttpMethod.GET, "/api/v1/product/menu").hasAnyRole("ADMIN", "CASHIER")


                        // ====================
                        // BARISTA
                        // ====================
                        .requestMatchers(HttpMethod.POST, "/v1/barista-order/orders/*/start").hasRole(Role.BARISTA.toString())
                        .requestMatchers(HttpMethod.POST, "/api/v1/barista-order/orders/*/done").hasRole(Role.BARISTA.toString())
                        .requestMatchers(HttpMethod.GET, "/api/v1/barista/orders").hasRole(Role.BARISTA.toString())


                        // ====================
                        // TEST
                        // ====================
                        .requestMatchers(HttpMethod.GET, "/api/v1/test/me").hasAnyRole(Role.ADMIN.toString(), Role.STAFF.toString(), Role.CASHIER.toString(), Role.BARISTA.toString())
                        .requestMatchers(HttpMethod.GET, "/api/v1/test/admin").hasRole(Role.ADMIN.toString())


                        // ====================
                        // WebSocket
                        // ====================
                        .requestMatchers("/ws/**").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    // ====================================================================================
    // Custom Error for different user role trying to access endpoints
    // ====================================================================================
    private void writeJson (HttpServletResponse response, HttpStatus status, Object body) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    // UNAUTHORIZED
    private AuthenticationEntryPoint authenticationEntryPoint () {
        return (request, response, authException) -> {
            ExceptionResponse<String> error = ExceptionResponse.<String>builder()
                    .message("Authentication Error")
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .timestamp(LocalDateTime.now())
                    .detail(authException.getMessage())
                    .build();

            writeJson(response, HttpStatus.UNAUTHORIZED, error);
        };
    }

    // FORBIDDEN
    private AccessDeniedHandler accessDeniedHandler () {
        return ((request, response, accessDeniedException) -> {
           ExceptionResponse<String> error = ExceptionResponse.<String>builder()
                   .message("Authorization Error")
                   .status(HttpStatus.FORBIDDEN.value())
                   .timestamp(LocalDateTime.now())
                   .detail(accessDeniedException.getMessage())
                   .build();

           writeJson(response, HttpStatus.FORBIDDEN, error);
        });
    }





    // AuthenticationManager (still needed)
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }


    // PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}


