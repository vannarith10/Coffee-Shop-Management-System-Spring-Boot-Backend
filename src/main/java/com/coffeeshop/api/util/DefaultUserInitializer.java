package com.coffeeshop.api.util;

import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.domain.enums.Schedule;
import com.coffeeshop.api.domain.enums.ShiftType;
import com.coffeeshop.api.domain.enums.Status;
import com.coffeeshop.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;


@Component
@RequiredArgsConstructor
class DefaultUserInitializer implements ApplicationRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;


    // Creating users
    @Override
    public void run(@NonNull ApplicationArguments args) {

        // I just added ShiftType and Schedule on Wednesday, 6th May 2026

        // User 1
        createUserIfNotExists(
                "Vyra Vannrith",
                "vyra.vannarith",
                "admin#1234",
                Role.ADMIN,
                ShiftType.FULL_DAY,
                List.of(Schedule.MONDAY, Schedule.TUESDAY, Schedule.WEDNESDAY, Schedule.FRIDAY, Schedule.SATURDAY, Schedule.SUNDAY)
        );

        // User 2
        createUserIfNotExists(
                "Lim Ansoleaphea",
                "lim.ansoleaphea",
                "cashier#1234",
                Role.CASHIER,
                ShiftType.MORNING,
                List.of(Schedule.MONDAY, Schedule.TUESDAY, Schedule.WEDNESDAY, Schedule.FRIDAY, Schedule.SATURDAY, Schedule.SUNDAY)
        );

        // User 3
        createUserIfNotExists(
                "Sareach Puthbormey",
                "sareach.puthbormey",
                "cashier#1234",
                Role.CASHIER,
                ShiftType.AFTERNOON,
                List.of(Schedule.MONDAY, Schedule.TUESDAY, Schedule.WEDNESDAY, Schedule.FRIDAY, Schedule.SATURDAY, Schedule.SUNDAY)
        );

        // User 4
        createUserIfNotExists(
                "Leum Sengheang",
                "leum.sengheang",
                "barista#1234",
                Role.BARISTA,
                ShiftType.FULL_DAY,
                List.of(Schedule.MONDAY, Schedule.TUESDAY, Schedule.WEDNESDAY, Schedule.FRIDAY, Schedule.SATURDAY, Schedule.SUNDAY)
        );
    }

    // Function to create default User
    private void createUserIfNotExists(String name,
                                       String username,
                                       String password,
                                       Role role,
                                       ShiftType shiftType,
                                       List<Schedule> schedules) {
        if(userRepo.existsByUsername(username)) return;

        User user = User.builder()
                .name(name)
                .username(username)
                .password(encoder.encode(password))
                .role(role)
                .status(Status.ACTIVE)
                .isActive(true)
                .shiftType(shiftType)
                .schedules(schedules)

                .createdAt(Instant.now())
                .build();

        userRepo.save(user);
    }

}
