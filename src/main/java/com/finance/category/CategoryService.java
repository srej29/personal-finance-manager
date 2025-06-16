package com.finance.category;


import com.finance.exception.ResourceNotFoundException;
import com.finance.transaction.TransactionRepository;  // ADD THIS IMPORT
import com.finance.user.User;
import com.finance.user.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final TransactionRepository transactionRepository;  // ADD THIS

    // UPDATE CONSTRUCTOR to include TransactionRepository
    public CategoryService(CategoryRepository categoryRepository, UserService userService, TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.userService = userService;
        this.transactionRepository = transactionRepository;  // ADD THIS
    }


    /**
     * Initializes default categories upon application startup.
     * This method runs once after the application context has been loaded.
     */
    @PostConstruct
    public void initDefaultCategories() {
        // Income categories
        if (categoryRepository.findByNameAndIsCustomFalse("Salary").isEmpty()) {
            categoryRepository.save(new Category("Salary", CategoryType.INCOME, false, null));
        }

        // Expense categories
        if (categoryRepository.findByNameAndIsCustomFalse("Food").isEmpty()) {
            categoryRepository.save(new Category("Food", CategoryType.EXPENSE, false, null));
        }
        if (categoryRepository.findByNameAndIsCustomFalse("Rent").isEmpty()) {
            categoryRepository.save(new Category("Rent", CategoryType.EXPENSE, false, null));
        }
        if (categoryRepository.findByNameAndIsCustomFalse("Transportation").isEmpty()) {
            categoryRepository.save(new Category("Transportation", CategoryType.EXPENSE, false, null));
        }
        if (categoryRepository.findByNameAndIsCustomFalse("Entertainment").isEmpty()) {
            categoryRepository.save(new Category("Entertainment", CategoryType.EXPENSE, false, null));
        }
        if (categoryRepository.findByNameAndIsCustomFalse("Healthcare").isEmpty()) {
            categoryRepository.save(new Category("Healthcare", CategoryType.EXPENSE, false, null));
        }
        if (categoryRepository.findByNameAndIsCustomFalse("Utilities").isEmpty()) {
            categoryRepository.save(new Category("Utilities", CategoryType.EXPENSE, false, null));
        }
    }

    /**
     * Creates a new custom category for a specific user.
     * Ensures the category name is unique for that user.
     * @param name The name of the custom category.
     * @param type The type of the category (INCOME/EXPENSE).
     * @param userId The ID of the user creating the category.
     * @return The created Category object.
     * @throws IllegalArgumentException if a custom category with the same name already exists for the user.
     * @throws ResourceNotFoundException if the user is not found.
     */
    @Transactional
    public Category createCustomCategory(String name, CategoryType type, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Check if a custom category with this name already exists for this user
        if (categoryRepository.existsByNameAndUserAndIsCustomTrue(name, user)) {
            throw new IllegalArgumentException("Custom category with name '" + name + "' already exists for this user.");
        }

        Category category = new Category(name, type, true, user);
        return categoryRepository.save(category);
    }

    /**
     * Retrieves all categories accessible to a given user (default and custom).
     * @param userId The ID of the user.
     * @return A list of categories.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public List<Category> getAllCategoriesForUser(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return categoryRepository.findByUserOrUserIsNull(user);
    }

    /**
     * Finds a category by its name, accessible by a specific user.
     * This checks for default categories or custom categories belonging to the user.
     * @param categoryName The name of the category to find.
     * @param userId The ID of the user.
     * @return An Optional containing the Category if found, or empty.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public Optional<Category> findAccessibleCategoryByName(String categoryName, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Use the correct repository method
        return categoryRepository.findAccessibleCategoryByNameAndUser(categoryName, user);
    }

    /**
     * Retrieves a category by its ID, ensuring it belongs to the specified user.
     * This is a secure way to access categories for update/delete operations.
     * @param categoryId The ID of the category.
     * @param userId The ID of the user.
     * @return An Optional containing the Category if found and owned by the user, or empty.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public Optional<Category> getCategoryByIdAndUser(Long categoryId, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return categoryRepository.findByIdAndUser(categoryId, user);
    }

    /**
     * Updates an existing custom category for a specific user.
     * Only custom categories can be updated.
     * @param categoryId The ID of the category to update.
     * @param newName The new name for the category.
     * @param newType The new type for the category (INCOME/EXPENSE).
     * @param userId The ID of the user attempting to update.
     * @return The updated Category object.
     * @throws ResourceNotFoundException if the category is not found for the user.
     * @throws IllegalArgumentException if the category is not custom or if the new name is a duplicate.
     */
    @Transactional
    public Category updateCategory(Long categoryId, String newName, CategoryType newType, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Category existingCategory = categoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId + " for user ID: " + userId));

        if (!existingCategory.isCustom()) {
            throw new IllegalArgumentException("Only custom categories can be updated or deleted.");
        }

        // Check for duplicate name if the name is changed and it's not the same category
        if (!existingCategory.getName().equals(newName) && categoryRepository.existsByNameAndUserAndIsCustomTrue(newName, user)) {
            throw new IllegalArgumentException("A custom category with name '" + newName + "' already exists for this user.");
        }

        existingCategory.setName(newName);
        existingCategory.setType(newType);

        return categoryRepository.save(existingCategory);
    }

    /**
     * Deletes a custom category for a specific user.
     * Default categories cannot be deleted.
     * @param categoryName The name of the custom category to delete.
     * @param userId The ID of the user trying to delete the category.
     * @throws ResourceNotFoundException if the category is not found or not custom for the user.
     * @throws IllegalArgumentException if the category is a default category.
     */
    @Transactional
    public void deleteCustomCategory(String categoryName, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // FIXED: Use the correct repository method you already have
        Category category = categoryRepository.findByNameAndUserAndIsCustomTrue(categoryName, user)
                .orElseThrow(() -> new ResourceNotFoundException("Custom category '" + categoryName + "' not found for this user."));

        // Check if category is in use - NOW THIS WILL WORK
        boolean isInUse = transactionRepository.existsByCategoryAndUser(category, user);
        if (isInUse) {
            throw new IllegalArgumentException("Cannot delete category '" + categoryName + "' as it is currently in use by transactions.");
        }

        // Delete the category
        categoryRepository.delete(category);
    }


    /**
     * Retrieves all default categories (isCustom = false).
     * @return A list of default Category objects.
     */
    public List<Category> getDefaultCategories() {
        return categoryRepository.findByIsCustomFalse();
    }

    public void validateCategoryType(String type) {
        if (!"INCOME".equals(type) && !"EXPENSE".equals(type)) {
            throw new IllegalArgumentException("Category type must be INCOME or EXPENSE");
        }
    }
}