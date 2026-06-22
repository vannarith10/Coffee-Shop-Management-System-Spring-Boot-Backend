package com.coffeeshop.api.service;

import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.dto.auth.LoginResponse;
import com.coffeeshop.api.dto.auth.RefreshAccessTokenResponse;

public interface AuthTokenService {

    RefreshAccessTokenResponse generateAccessTokenFromRefreshToken(String refreshToken);

    LoginResponse generateTokenResponse (User user);

}
