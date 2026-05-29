package com.coffeeshop.api.service;

import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.dto.AccessTokenResponse;
import com.coffeeshop.api.dto.auth.LoginResponse;

public interface AuthTokenService {

    AccessTokenResponse generateAccessTokenFromRefreshToken(String refreshToken);

    LoginResponse generateTokenResponse (User user);

}
