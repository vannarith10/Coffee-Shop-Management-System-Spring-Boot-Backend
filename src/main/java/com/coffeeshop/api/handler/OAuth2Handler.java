package com.coffeeshop.api.handler;

import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.domain.enums.ShiftType;
import com.coffeeshop.api.domain.enums.Status;
import com.coffeeshop.api.dto.auth.LoginResponse;
import com.coffeeshop.api.repository.UserRepository;
import com.coffeeshop.api.service.AuthTokenService;
import com.coffeeshop.api.service.OAuth2CodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;


@Component
@RequiredArgsConstructor
public class OAuth2Handler extends SimpleUrlAuthenticationSuccessHandler {


    private final UserRepository userRepository;
    private static final Instant CAMBODIA_TIME_NOW = ZonedDateTime.now(ZoneId.of("Asia/Phnom_Penh")).toInstant();
    private final OAuth2CodeService oAuth2CodeService;
    private final AuthTokenService authTokenService;

    private static final String FRONTEND_BASE_URL = "http://localhost:5173";
    private static final String FRONTEND_LOGIN_PATH = "/login";



    //===================================
    // OAUTH2 SUCCESS HANDLER
    //===================================
    public AuthenticationSuccessHandler oAuth2SuccessHandle () {
        return (request, response, authentication) -> {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauthToken.getPrincipal();

            String provider = oauthToken.getAuthorizedClientRegistrationId();
            String providerId = oauth2User.getName();
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");

            User user = userRepository.findUserByProviderAndProviderId(provider, providerId)
                    .or(() -> userRepository.findByEmail(email))
                    .orElseGet(() -> {
                        User newUser = User.builder()
                                .name(name != null ? name : "Google User")
                                .email(email)
                                .username(email)

                                .provider(provider)
                                .providerId(providerId)
                                .role(Role.STAFF)

                                .isActive(true)
                                .status(Status.ACTIVE)
                                .createdAt(CAMBODIA_TIME_NOW)
                                .shiftType(ShiftType.FULL_DAY)

                                .schedules(new ArrayList<>())

                                .build();
                        return userRepository.save(newUser);
                    });

            if (user.getProviderId() == null) {
                user.setProviderId(providerId);
                user.setProvider(provider);
                userRepository.save(user);
            }

            if (!user.isActive()) {
                response.sendRedirect(FRONTEND_BASE_URL + FRONTEND_LOGIN_PATH);
                return;
            }

            if (user.getStatus() == Status.INACTIVE || user.getStatus() == Status.SUSPENDED) {
                response.sendRedirect(FRONTEND_BASE_URL + FRONTEND_LOGIN_PATH);
                return;
            }



            // TOKEN FLOW
            LoginResponse loginResponse = authTokenService.generateTokenResponse(user);

            // STORE and REDIRECT
            String code = oAuth2CodeService.store(loginResponse);
            response.sendRedirect("http://localhost:5173/oauth2/redirect?code=" + code);
        };
    }



    //===================================
    // OAUTH2 FAILURE HANDLER
    //===================================
    public AuthenticationFailureHandler oAuth2FailureHandler () {
        return ((request, response, exception) -> {
           response.sendRedirect(FRONTEND_BASE_URL + FRONTEND_LOGIN_PATH);
        });
    }

}



















