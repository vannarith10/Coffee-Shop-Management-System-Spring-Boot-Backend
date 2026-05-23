package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.AccessTokenResponse;

public interface AuthTokenService {

    AccessTokenResponse generateAccessTokenFromRefreshToken(String refreshToken);

}
