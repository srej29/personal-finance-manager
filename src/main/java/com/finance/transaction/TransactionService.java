package com.finance.transaction;

import com.finance.category.Category;
import com.finance.category.CategoryService;
import com.finance.category.CategoryType;
import com.finance.exception.ResourceNotFoundException;
import com.finance.user.User;
import com.finance.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    public TransactionService(TransactionRepository transactionRepository, UserService userService, CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    /**
     * Creates a new transaction for a user.
     * Validates amount, date, and category existence.
     * @param amount The transaction amount.
     * @param date The transaction date.
     * @param categoryName The name of the category.
     * @param description The transaction description.
     * @param userId The ID of the user creating the transaction.
     * @return The created Transaction object.
     * @throws IllegalArgumentException if amount is not positive, date is in future, or category is invalid.
     * @throws ResourceNotFoundException if the user or category is not found.
     */
    @Transactional
    public Transaction createTransaction(BigDecimal amount, LocalDate date, String categoryName, String description, Long userId) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be a positive decimal value.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Transaction date cannot be in the future.");
        }
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be a positive decimal value.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Transaction date cannot be in the future.");
        }

        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Category category = categoryService.findAccessibleCategoryByName(categoryName, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or not accessible: " + categoryName));

        // Important: Ensure the category is not soft-deleted or invalid if we were to implement soft deletes.
        // For now, if found, it's considered valid.

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDate(date);
        transaction.setCategory(category);
        transaction.setDescription(description);
        transaction.setUser(user);

        return transactionRepository.save(transaction);
    }

    /**
     * Retrieves a specific transaction by its ID for a given user.
     * Ensures data isolation.
     * @param transactionId The ID of the transaction.
     * @param userId The ID of the user.
     * @return An Optional containing the transaction if found and owned by the user, or empty.
     */
    public Optional<Transaction> getTransactionById(Long transactionId, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return transactionRepository.findByIdAndUser(transactionId, user);
    }

    /**
     * Retrieves all transactions for a specific user, with optional filtering.
     * @param userId The ID of the user.
     * @param startDate Optional start date for filtering.
     * @param endDate Optional end date for filtering.
     * @param categoryName Optional category name for filtering.
     * @param categoryType Optional category type (INCOME/EXPENSE) for filtering.
     * @return A list of transactions.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public List<Transaction> getFilteredTransactions(Long userId, LocalDate startDate, LocalDate endDate, String categoryName, CategoryType categoryType) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // If a category name is provided, ensure it's accessible to the user
        if (categoryName != null && !categoryName.isEmpty()) {
            categoryService.findAccessibleCategoryByName(categoryName, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found or not accessible: " + categoryName));
        }

        return transactionRepository.findFilteredTransactions(user, startDate, endDate, categoryName, categoryType);
    }

    /**
     * Updates an existing transaction for a user.
     * All fields except date can be modified.
     * Validates amount, category existence.
     * @param transactionId The ID of the transaction to update.
     * @param amount The new amount (optional).
     * @param categoryName The new category name (optional).
     * @param description The new description (optional).
     * @param userId The ID of the user performing the update.
     * @return The updated Transaction object.
     * @throws ResourceNotFoundException if the transaction or user/category is not found.
     * @throws IllegalArgumentException if amount is not positive.
     */
    @Transactional
    public Transaction updateTransaction(Long transactionId, BigDecimal amount, String categoryName, String description, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Transaction transaction = transactionRepository.findByIdAndUser(transactionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + transactionId + " for user: " + userId));

        if (amount != null) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be a positive decimal value.");
            }
            transaction.setAmount(amount);
        }

        if (categoryName != null && !categoryName.isEmpty()) {
            Category newCategory = categoryService.findAccessibleCategoryByName(categoryName, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found or not accessible: " + categoryName));
            transaction.setCategory(newCategory);
        }

        if (description != null) {
            transaction.setDescription(description);
        }

        // KEY FIX: Never update the date field - keep original date
        // Even if date is passed in request, ignore it completely

        return transactionRepository.save(transaction);
    }

    /**
     * Deletes a transaction for a user.
     * Ensures data isolation.
     * @param transactionId The ID of the transaction to delete.
     * @param userId The ID of the user performing the deletion.
     * @throws ResourceNotFoundException if the transaction or user is not found.
     */
    @Transactional
    public void deleteTransaction(Long transactionId, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Transaction transaction = transactionRepository.findByIdAndUser(transactionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + transactionId + " for user: " + userId));

        transactionRepository.delete(transaction);
    }


}
