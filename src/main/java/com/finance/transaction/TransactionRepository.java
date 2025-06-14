package com.finance.transaction;

import com.finance.category.Category;
import com.finance.category.CategoryType;
import com.finance.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Transaction entities.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Checks if any transactions exist for a given category and user.
     * This is used before deleting a category to ensure data integrity.
     * @param category The Category to check for.
     * @param user The User to check for (ensures user owns the transactions).
     * @return true if transactions exist, false otherwise.
     */
    boolean existsByCategoryAndUser(Category category, User user);

    /**
     * Finds a transaction by its ID and ensures it belongs to a specific user.
     * This ensures data isolation between users.
     * @param id The ID of the transaction.
     * @param user The user who should own the transaction.
     * @return An Optional containing the transaction if found and owned by the user.
     */
    Optional<Transaction> findByIdAndUser(Long id, User user);

    /**
     * Finds transactions for a user with optional filtering.
     * @param user The user whose transactions to retrieve.
     * @param startDate Optional start date filter.
     * @param endDate Optional end date filter.
     * @param categoryName Optional category name filter.
     * @param categoryType Optional category type filter.
     * @return A list of filtered transactions ordered by date descending.
     */
    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
            "AND (:startDate IS NULL OR t.date >= :startDate) " +
            "AND (:endDate IS NULL OR t.date <= :endDate) " +
            "AND (:categoryName IS NULL OR t.category.name = :categoryName) " +
            "AND (:categoryType IS NULL OR t.category.type = :categoryType) " +
            "ORDER BY t.date DESC")
    List<Transaction> findFilteredTransactions(@Param("user") User user,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate,
                                               @Param("categoryName") String categoryName,
                                               @Param("categoryType") CategoryType categoryType);

    /**
     * Sums the amounts of transactions for a user within a date range and category type.
     * Used for generating reports (income vs expenses).
     * @param user The user whose transactions to sum.
     * @param startDate The start date (inclusive).
     * @param endDate The end date (inclusive).
     * @param categoryType The category type to filter by (INCOME or EXPENSE).
     * @return The sum of transaction amounts, or BigDecimal.ZERO if no transactions found.
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user " +
            "AND t.date >= :startDate AND t.date <= :endDate " +
            "AND t.category.type = :categoryType")
    BigDecimal sumAmountByUserAndDateRangeAndCategoryType(@Param("user") User user,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate,
                                                          @Param("categoryType") CategoryType categoryType);

    /**
     * Finds all transactions for a specific user ordered by date descending.
     * @param user The user whose transactions to retrieve.
     * @return A list of transactions for the user.
     */
    List<Transaction> findByUserOrderByDateDesc(User user);

    /**
     * Finds transactions for a user within a date range.
     * @param user The user whose transactions to retrieve.
     * @param startDate The start date (inclusive).
     * @param endDate The end date (inclusive).
     * @return A list of transactions within the date range ordered by date descending.
     */
    List<Transaction> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate startDate, LocalDate endDate);

    /**
     * Finds transactions for a user by category.
     * @param user The user whose transactions to retrieve.
     * @param category The category to filter by.
     * @return A list of transactions for the specified category ordered by date descending.
     */
    List<Transaction> findByUserAndCategoryOrderByDateDesc(User user, Category category);

    /**
     * Calculates total income minus total expenses for a user within a date range.
     * Used for savings goal progress calculation.
     * @param user The user whose net savings to calculate.
     * @param startDate The start date (inclusive).
     * @param endDate The end date (inclusive).
     * @return The net savings (income - expenses) for the period.
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN t.category.type = 'INCOME' THEN t.amount ELSE -t.amount END), 0) " +
            "FROM Transaction t WHERE t.user = :user " +
            "AND t.date >= :startDate AND t.date <= :endDate")
    BigDecimal calculateNetSavingsByUserAndDateRange(@Param("user") User user,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * Finds transactions for a user by category type (INCOME or EXPENSE).
     * @param user The user whose transactions to retrieve.
     * @param categoryType The category type to filter by.
     * @return A list of transactions for the specified category type ordered by date descending.
     */
    List<Transaction> findByUserAndCategoryTypeOrderByDateDesc(User user, CategoryType categoryType);

    /**
     * Counts the number of transactions for a user.
     * @param user The user whose transactions to count.
     * @return The total number of transactions for the user.
     */
    long countByUser(User user);

    /**
     * Counts the number of transactions for a user within a date range.
     * @param user The user whose transactions to count.
     * @param startDate The start date (inclusive).
     * @param endDate The end date (inclusive).
     * @return The number of transactions within the date range.
     */
    long countByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

    /**
     * Finds the most recent transaction for a user.
     * @param user The user whose most recent transaction to find.
     * @return An Optional containing the most recent transaction, or empty if no transactions exist.
     */
    Optional<Transaction> findTopByUserOrderByDateDescIdDesc(User user);

    /**
     * Finds transactions for a user with amounts greater than or equal to a specified value.
     * Useful for finding large transactions or filtering by minimum amount.
     * @param user The user whose transactions to retrieve.
     * @param minAmount The minimum amount threshold.
     * @return A list of transactions with amount >= minAmount ordered by amount descending.
     */
    List<Transaction> findByUserAndAmountGreaterThanEqualOrderByAmountDesc(User user, BigDecimal minAmount);

    /**
     * Finds the largest transaction (by amount) for a user within a date range.
     * @param user The user whose largest transaction to find.
     * @param startDate The start date (inclusive).
     * @param endDate The end date (inclusive).
     * @return An Optional containing the largest transaction, or empty if no transactions exist.
     */
    Optional<Transaction> findTopByUserAndDateBetweenOrderByAmountDesc(User user, LocalDate startDate, LocalDate endDate);

    /**
     * Gets monthly spending totals for a user in a given year.
     * Returns a list of objects with month number and total amount.
     * @param user The user whose monthly totals to calculate.
     * @param year The year to analyze.
     * @param categoryType The category type to filter by (EXPENSE for spending analysis).
     * @return A list of monthly totals.
     */
    @Query("SELECT MONTH(t.date) as month, SUM(t.amount) as total " +
            "FROM Transaction t WHERE t.user = :user " +
            "AND YEAR(t.date) = :year " +
            "AND t.category.type = :categoryType " +
            "GROUP BY MONTH(t.date) " +
            "ORDER BY MONTH(t.date)")
    List<Object[]> findMonthlyTotalsByUserAndYearAndCategoryType(@Param("user") User user,
                                                                 @Param("year") int year,
                                                                 @Param("categoryType") CategoryType categoryType);

    /**
     * Gets yearly totals for a user grouped by category type.
     * @param user The user whose yearly totals to calculate.
     * @param year The year to analyze.
     * @return A list of category types and their totals.
     */
    @Query("SELECT t.category.type, SUM(t.amount) " +
            "FROM Transaction t WHERE t.user = :user " +
            "AND YEAR(t.date) = :year " +
            "GROUP BY t.category.type")
    List<Object[]> findYearlyTotalsByUserAndYear(@Param("user") User user, @Param("year") int year);

    /**
     * Finds transactions by description containing a search term (case-insensitive).
     * @param user The user whose transactions to search.
     * @param searchTerm The term to search for in descriptions.
     * @return A list of transactions with descriptions containing the search term.
     */
    List<Transaction> findByUserAndDescriptionContainingIgnoreCaseOrderByDateDesc(User user, String searchTerm);

    /**
     * Deletes all transactions for a specific user and category.
     * This is a custom delete method that ensures data isolation.
     * @param user The user whose transactions to delete.
     * @param category The category of transactions to delete.
     */
    void deleteByUserAndCategory(User user, Category category);

    /**
     * Checks if a user has any transactions after a specific date.
     * Useful for validation before account closure or data archiving.
     * @param user The user to check.
     * @param date The date to check after.
     * @return true if transactions exist after the date, false otherwise.
     */
    boolean existsByUserAndDateAfter(User user, LocalDate date);
}