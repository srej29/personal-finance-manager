package com.finance.category;

import com.finance.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a custom category by name for a specific user.
     * @param name The name of the category.
     * @param user The user who owns the category.
     * @return An Optional containing the Category if found, or empty.
     */
    Optional<Category> findByNameAndUserAndIsCustomTrue(String name, User user);

    /**
     * Finds a default category by its name.
     * @param name The name of the default category.
     * @return An Optional containing the Category if found, or empty.
     */
    Optional<Category> findByNameAndIsCustomFalse(String name);

    /**
     * Finds all categories (default and custom) for a given user.
     * Includes default categories (where user is null) and custom categories belonging to the user.
     * @param user The user.
     * @return A list of categories accessible by the user.
     */
    List<Category> findByUserOrUserIsNull(User user);

    /**
     * Checks if a custom category with the given name already exists for a specific user.
     * @param name The name of the category to check.
     * @param user The user to whom the category belongs.
     * @return True if a custom category with the name exists for the user, false otherwise.
     */
    boolean existsByNameAndUserAndIsCustomTrue(String name, User user);

    /**
     * Finds a category by its ID and ensures it belongs to a specific user.
     * This is crucial for securing category operations (update/delete) to prevent users from affecting
     * categories they don't own.
     * @param id The ID of the category.
     * @param user The user object to which the category must belong.
     * @return An Optional containing the Category if found and owned by the user, or empty.
     */
    Optional<Category> findByIdAndUser(Long id, User user);

    /**
     * Finds all categories that are marked as default (isCustom = false).
     * @return A list of default categories.
     */
    List<Category> findByIsCustomFalse();

    /**
     * Alternative method to find accessible categories using a query.
     * This finds categories that are either default (user is null) or belong to the specific user.
     * @param user The user.
     * @return A list of accessible categories.
     */
    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user = :user ORDER BY c.type, c.name")
    List<Category> findAccessibleCategoriesForUser(@Param("user") User user);

    /**
     * Finds a category by name that is accessible to a user.
     * This checks for both custom categories belonging to the user and default categories.
     * Used by CategoryService.findAccessibleCategoryByName() method.
     * @param name The name of the category.
     * @param user The user.
     * @return An Optional containing the Category if found, or empty.
     */
    @Query("SELECT c FROM Category c WHERE c.name = :name AND " +
            "(c.user = :user OR c.user IS NULL)")
    Optional<Category> findAccessibleCategoryByNameAndUser(@Param("name") String name,
                                                           @Param("user") User user);
}