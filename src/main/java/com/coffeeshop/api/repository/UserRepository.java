package com.coffeeshop.api.repository;

import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    Optional<User> findByUsernameIgnoreCase(String username);

    List<User> findAllByIsActiveFalse();

    boolean existsByRole(Role role);

    List<User> findByRole(Role role);

    List<User> findAllByRole(Role role);


    @Query("""
        SELECT u FROM User u
        ORDER BY
            CASE u.role
                WHEN 'ADMIN' THEN 1
                WHEN 'CASHIER' THEN 2
                WHEN 'BARISTA' THEN 3
                WHEN 'STAFF' THEN 4
            END
    """)
    Page<User> findAllByRolePriority (Pageable pageable);



    Optional<User> findByEmail(String email);

    Optional<User> findUserByProviderAndProviderId(String provider, String providerId);

}
