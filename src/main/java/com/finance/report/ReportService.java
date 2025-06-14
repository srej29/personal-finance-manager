package com.finance.report;

import com.finance.category.Category;
import com.finance.category.CategoryType;
import com.finance.exception.ResourceNotFoundException;
import com.finance.report.dto.CategorySpendingReport;
import com.finance.report.dto.IncomeExpenseSummary;
import com.finance.transaction.Transaction;
import com.finance.transaction.TransactionRepository;
import com.finance.user.User;
import com.finance.user.UserService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public ReportService(TransactionRepository transactionRepository, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    /**
     * Generates a summary of total income, total expenses, and net balance for a given date range.
     * @param userId The ID of the user.
     * @param startDate The start date of the report period.
     * @param endDate The end date of the report period.
     * @return An IncomeExpenseSummary DTO.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public IncomeExpenseSummary getIncomeExpenseSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Use the sumAmountByUserAndDateRangeAndCategoryType from TransactionRepository
        BigDecimal totalIncome = transactionRepository.sumAmountByUserAndDateRangeAndCategoryType(user, startDate, endDate, CategoryType.INCOME);
        BigDecimal totalExpenses = transactionRepository.sumAmountByUserAndDateRangeAndCategoryType(user, startDate, endDate, CategoryType.EXPENSE);

        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        return new IncomeExpenseSummary(totalIncome, totalExpenses, netBalance);
    }

    /**
     * Generates a spending analysis report by category for a given date range.
     * Only includes categories with transactions within the range.
     * @param userId The ID of the user.
     * @param startDate The start date of the report period.
     * @param endDate The end date of the report period.
     * @return A list of CategorySpendingReport DTOs, sorted by total amount descending.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public List<CategorySpendingReport> getSpendingAnalysisByCategory(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Get all transactions for the user within the date range
        List<Transaction> transactions = transactionRepository.findFilteredTransactions(user, startDate, endDate, null, null);

        // Group transactions by category and sum their amounts
        Map<Category, BigDecimal> categoryTotals = transactions.stream()
                .filter(t -> t.getCategory().getType() == CategoryType.EXPENSE) // Only consider expenses for spending analysis
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.mapping(Transaction::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        // Convert the map to a list of CategorySpendingReport DTOs
        return categoryTotals.entrySet().stream()
                .map(entry -> new CategorySpendingReport(
                        entry.getKey().getName(),
                        entry.getKey().getType(),
                        entry.getValue()
                ))
                .sorted((r1, r2) -> r2.getTotalAmount().compareTo(r1.getTotalAmount())) // Sort by total amount descending
                .collect(Collectors.toList());
    }

    // Goal progress reports are implicitly handled by fetching individual goals (GET /api/goals/{id})
    // or all goals (GET /api/goals), as the GoalResponse DTO includes progressPercentage and remainingAmount.
    // A summary of all goals with their progress could be added here if needed, but for now,
    // the existing Goal API covers individual goal progress.
}
