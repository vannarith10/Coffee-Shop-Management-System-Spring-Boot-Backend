package com.coffeeshop.api.domain;

import com.coffeeshop.api.domain.enums.Role;
import com.coffeeshop.api.domain.enums.Schedule;
import com.coffeeshop.api.domain.enums.ShiftType;
import com.coffeeshop.api.domain.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(nullable = false, length = 50)
    private String name;


    @Column(nullable = true, unique = true, length = 50)    // Nullable for OAuth2 users
    private String username;


    @Column(nullable = true)    // OAuth2 doesn't come with password, so it can be null
    private String password;


    @Column(unique = true)  // Required unique for OAuth2
    private String email;


    @Column(nullable = false, length = 20)
    @Builder.Default    // ensure Lombok's builder respects my default initialization 'local' for users without OAuth2
    private String provider = "local";  // default 'local' for existing rows


    @Column(name = "provider_id", length = 255)
    private String providerId;


    @Enumerated(EnumType.STRING)    // Store enum as text
    @Column(nullable = false, length = 20)
    private Role role;


    @Column(nullable = false)
    private boolean isActive;


    @Enumerated(EnumType.STRING)    // Store enum as text
    @Column(nullable = false)
    private Status status; // ACTIVE, INACTIVE, SUSPENDED


    @Column(nullable = false, updatable = false)
    private Instant createdAt;


    private Instant updatedAt;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)  // MORNING, AFTERNOON, FULL_DAY
    private ShiftType shiftType;


    // List of Enum won't work as well as a single Enum
    @ElementCollection(targetClass = Schedule.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_schedules",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_day", nullable = false, length = 10)
    private List<Schedule> schedules;


    private String imageKey;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_profile_id")
    private ShopProfile shopProfile;

}












