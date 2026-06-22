package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.auth.LoginResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


// Temporary authorization code store for OAuth2 Authorization Code Grant flow.
@Service
public class OAuth2CodeService {

    // Store authorization code in memory
    private final ConcurrentHashMap<String, LoginResponse> store = new ConcurrentHashMap<>();


    // Background thread that auto-deletes expired codes after 2 minutes
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();


    // Stores a LoginResponse and returns a temporary authorization code.
    // The code auto-expires after 2 minutes.
    public String store (LoginResponse response) {
        String code = UUID.randomUUID().toString();
        store.put(code, response);
        executor.schedule(() -> store.remove(code), 2, TimeUnit.MINUTES);
        return code;
    }


    // Retrieves and consumes an authorization code.
    // ONE-TIME USE: Code is deleted immediately upon retrieval.
    public Optional<LoginResponse> retrieve (String code) {
        return Optional.ofNullable(store.remove(code));
    }
}
