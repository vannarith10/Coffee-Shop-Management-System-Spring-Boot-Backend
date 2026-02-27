package com.coffeeshop.api.serviceimpl.payment_test;

import com.coffeeshop.api.config.payment_test.PaywayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CallbackSecurityService {

    private final PaywayProperties props;

    public void verifyToke (String token) {
        if(!StringUtils.hasText(token) || !token.equals(props.getCallbackToken())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Invalid callback token");
        }
    }

}
