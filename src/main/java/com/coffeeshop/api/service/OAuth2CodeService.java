package com.coffeeshop.api.service;

import com.coffeeshop.api.dto.auth.LoginResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class OAuth2CodeService {

    private final ConcurrentHashMap<String, LoginResponse> store = new ConcurrentHashMap<>();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();


    public String store (LoginResponse response) {
        String code = UUID.randomUUID().toString();

        store.put(code, response);
        executor.schedule(() -> store.remove(code), 2, TimeUnit.MINUTES);
        return code;
    }


    public Optional<LoginResponse> retrieve (String code) {
        return Optional.ofNullable(store.remove(code));
    }
}
