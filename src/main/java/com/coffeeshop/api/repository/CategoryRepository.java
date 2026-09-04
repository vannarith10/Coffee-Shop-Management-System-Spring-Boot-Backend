package com.coffeeshop.api.repository;

import com.coffeeshop.api.domain.Category;
import com.coffeeshop.api.domain.enums.CategoryType;
import com.coffeeshop.api.dto.category.CategoryNameAndTypeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByTypeAndName(CategoryType type, String name);

    boolean existsByName (String name);

    List<Category> findByActiveTrue();

    Optional<Category> findByNameIgnoreCase(String name);

    @Override
    Page<Category> findAll(Pageable pageable);



    long countByType(CategoryType type);

    long countByActiveFalse();

    @Query("""
        SELECT c.name FROM Category c
    """)
    List<String> findAllNames();


    @Query("""
        SELECT new com.coffeeshop.api.dto.category.CategoryNameAndTypeResponse(
                c.id,
                c.name,
                c.type
            )
        FROM Category c
        ORDER BY c.name ASC
    """)
    List<CategoryNameAndTypeResponse> findAllNamesAndTypes ();


    Page<Category> findAllByOrderByNameAsc(Pageable pageable);

}
