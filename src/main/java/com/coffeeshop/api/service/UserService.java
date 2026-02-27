package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.*;
import com.coffeeshop.api.dto.auth.*;

import java.util.List;
import java.util.UUID;

public interface UserService {

    RegisterResponse createAccount(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void setNewPassword (NewPasswordRequest request);

    void disableUser (DisableUserRequest request);

    void enableUser (EnableUserRequest request);

    List<DisableUserResponse> getAllDisabledUsers ();

    UUID getCurrentUserId();

    String getCurrentUsername();

}
