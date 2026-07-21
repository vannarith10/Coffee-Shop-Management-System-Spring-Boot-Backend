package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.*;
import com.coffeeshop.api.dto.auth.*;

import java.util.List;
import java.util.UUID;

public interface UserService {

    LoginResponse login(LoginRequest request);

    UUID getCurrentUserId();

    String getCurrentUsername();

}
