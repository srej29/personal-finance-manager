package com.finance.category;

import com.finance.user.User; // Import User entity for association
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "categories", uniqueConstraints = {
        // Ensure category name is unique per user for custom categories
        // Default categories will not be associated with a user, their names are globally unique
        @UniqueConstraint(columnNames = {"name", "user_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type; // INCOME or EXPENSE

    @Column(nullable = false)
    private boolean isCustom; // True for custom categories, false for default

    // A category can belong to a specific user (for custom categories) or be null (for default categories)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Foreign key column in 'categories' table
    private User user;

    public Category(String name, CategoryType type, boolean isCustom, User user) {
        this.name = name;
        this.type = type;
        this.isCustom = isCustom;
        this.user = user;
    }
}
