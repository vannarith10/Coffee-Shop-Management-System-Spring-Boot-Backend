package com.coffeeshop.api.websocket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

import java.security.Principal;



@Getter
@RequiredArgsConstructor
public class StompPrincipal implements Principal {

    private final String name;
    private final Authentication authentication;

}

