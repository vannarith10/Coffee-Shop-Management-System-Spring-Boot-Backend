package com.coffeeshop.api.domain;

import com.coffeeshop.api.domain.enums.CategoryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Locale;
import java.util.UUID;

@Entity
@Table(
        name = "categories",
        // The same Category Type with Name cannot exist for the second time.
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"type", "name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type; // FOOD / DRINK

    @Column(nullable = false)
    @NotBlank
    private String name; // Category name (e.g. COFFEE, TEA, BREAD)

    @Column(nullable = false)
    private boolean active; // Active = False if it runs out of ingredients (No more ice, DRINK is unactive)


    // Prevent case-sensitive name
    @PrePersist
    @PreUpdate
    public void normalize() {
        if (this.name != null) {
            this.name = this.name.trim().toUpperCase(Locale.ROOT);
        }
    }


}

