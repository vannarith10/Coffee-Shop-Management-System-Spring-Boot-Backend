package com.coffeeshop.api.util;

import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.domain.enums.Status;
import com.coffeeshop.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.Instant;


@Component
@RequiredArgsConstructor
class DefaultUserInitializer implements ApplicationRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    // Function to create default User
    private void createUserIfNotExists(String name,
                                       String username,
                                       String password,
                                       Role role) {
        if(userRepo.existsByUsername(username)) return;

        User user = User.builder()
                .name(name)
                .username(username)
                .password(encoder.encode(password))
                .role(role)
                .status(Status.ACTIVE)
                .isActive(true)
                .createdAt(Instant.now())
                .build();

        userRepo.save(user);
    }



    // Creating users
    @Override
    public void run(@NonNull ApplicationArguments args) {

        // User 1
        createUserIfNotExists(
                "Vyra Vannrith",
                "vyra.vannarith",
                "admin#1234",
                Role.ADMIN
        );

        // User 2
        createUserIfNotExists(
                "Lim Ansoleaphea",
                "lim.ansoleaphea",
                "cashier#1234",
                Role.CASHIER
        );

        // User 3
        createUserIfNotExists(
                "Sareach Puthbormey",
                "sareach.puthbormey",
                "cashier#1234",
                Role.CASHIER
        );

        // User 4
        createUserIfNotExists(
                "Leum Sengheang",
                "leum.sengheang",
                "barista#1234",
                Role.BARISTA
        );
    }

}
