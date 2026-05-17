package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.auth.LoginRequest;
import com.coffeeshop.api.dto.auth.LoginResponse;

import java.util.UUID;

public interface UserAuthService {

    LoginResponse login (LoginRequest request);

    UUID getCurrentUserId ();

    String getCurrentUsername ();

}
