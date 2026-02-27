package com.coffeeshop.api.repository;

import com.coffeeshop.api.domain.User;
import com.coffeeshop.api.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

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

}
