package com.coffeeshop.api.util;

import com.coffeeshop.api.domain.ShopProfile;
import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.domain.enums.Schedule;
import com.coffeeshop.api.domain.enums.ShiftType;
import com.coffeeshop.api.domain.enums.Status;
import com.coffeeshop.api.repository.ShopProfileRepository;
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
    private final ShopProfileRepository shopProfileRepository;


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

        // Add Shop Profile
        addShopProfileToAdminIfNull();
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

    // Shop Profile
    private void addShopProfileToAdminIfNull() {

        // 1. Create or get existing shop profile
        ShopProfile profile = shopProfileRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> {
                    ShopProfile newProfile = ShopProfile.builder()
                            .name("RUPP COFFEE")
                            .contactNumber("010 369 2026")
                            .address("Russian Federation Blvd (110), Phnom Penh 120404")
                            .description("A modern coffee shop in Phnom Penh serving premium coffee, handcrafted drinks, and fresh pastries in a comfortable and welcoming atmosphere.")
                            .region("Asia/Phnom_Penh")
                            .build();

                    return shopProfileRepository.save(newProfile);
                });

        // 2. Find all admins
        List<User> admins = userRepo.findAllByRole(Role.ADMIN);

        // 3. Assign shop profile to admins if missing
        List<User> updatedAdmins = admins.stream()
                .filter(user -> user.getShopProfile() == null)
                .peek(user -> {
                    user.setShopProfile(profile);
                    user.setUpdatedAt(Instant.now());
                })
                .toList();

        if (!updatedAdmins.isEmpty()) {
            userRepo.saveAll(updatedAdmins);
        }
    }

}
