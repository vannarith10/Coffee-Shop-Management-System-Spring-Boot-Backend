package com.coffeeshop.api.service.oldService;

import com.coffeeshop.api.dto.AccessTokenResponse;

public interface AuthTokenService {

    AccessTokenResponse generateAccessTokenFromRefreshToken(String refreshToken);

}
