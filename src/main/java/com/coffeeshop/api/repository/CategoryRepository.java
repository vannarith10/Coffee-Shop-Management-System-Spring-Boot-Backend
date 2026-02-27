package com.coffeeshop.api.repository;

import com.coffeeshop.api.domain.Category;
import com.coffeeshop.api.domain.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByTypeAndName(CategoryType type, String name);

    List<Category> findByActiveTrue();

    Optional<Category> findByNameIgnoreCase(String name);

}
