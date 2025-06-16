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
import java.util.HashMap;
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
     * Generates a summary with categorized income/expenses for monthly and yearly reports.
     */
    public IncomeExpenseSummary getIncomeExpenseSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        try {
            System.out.println("=== GENERATING INCOME EXPENSE SUMMARY ===");
            System.out.println("User ID: " + userId);
            System.out.println("Start Date: " + startDate);
            System.out.println("End Date: " + endDate);

            User user = userService.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

            // Get all transactions for the period
            List<Transaction> transactions = transactionRepository.findFilteredTransactions(
                    user, startDate, endDate, null, null);

            System.out.println("Found " + transactions.size() + " transactions");

            // Group income by category
            Map<String, BigDecimal> totalIncome = transactions.stream()
                    .filter(t -> t.getCategory().getType() == CategoryType.INCOME)
                    .collect(Collectors.groupingBy(
                            t -> t.getCategory().getName(),
                            HashMap::new,
                            Collectors.mapping(Transaction::getAmount,
                                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                    ));

            // Group expenses by category
            Map<String, BigDecimal> totalExpenses = transactions.stream()
                    .filter(t -> t.getCategory().getType() == CategoryType.EXPENSE)
                    .collect(Collectors.groupingBy(
                            t -> t.getCategory().getName(),
                            HashMap::new,
                            Collectors.mapping(Transaction::getAmount,
                                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                    ));

            // Calculate net savings
            BigDecimal totalIncomeSum = totalIncome.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalExpenseSum = totalExpenses.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal netSavings = totalIncomeSum.subtract(totalExpenseSum);

            System.out.println("Total Income: " + totalIncomeSum);
            System.out.println("Total Expenses: " + totalExpenseSum);
            System.out.println("Net Savings: " + netSavings);
            System.out.println("Income Categories: " + totalIncome);
            System.out.println("Expense Categories: " + totalExpenses);
            System.out.println("=========================================");

            return new IncomeExpenseSummary(totalIncome, totalExpenses, netSavings);

        } catch (Exception e) {
            System.err.println("Error in getIncomeExpenseSummary: " + e.getMessage());
            e.printStackTrace();
            // Return empty summary instead of throwing
            return new IncomeExpenseSummary(new HashMap<>(), new HashMap<>(), BigDecimal.ZERO);
        }
    }

    /**
     * Generates a monthly report with proper structure.
     */
    public IncomeExpenseSummary getMonthlyReport(Long userId, int year, int month) {
        try {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            IncomeExpenseSummary summary = getIncomeExpenseSummary(userId, startDate, endDate);
            summary.setMonth(month);
            summary.setYear(year);

            return summary;
        } catch (Exception e) {
            System.err.println("Error in getMonthlyReport: " + e.getMessage());
            e.printStackTrace();
            // Return empty monthly report
            IncomeExpenseSummary emptySummary = new IncomeExpenseSummary(new HashMap<>(), new HashMap<>(), BigDecimal.ZERO);
            emptySummary.setMonth(month);
            emptySummary.setYear(year);
            return emptySummary;
        }
    }

    /**
     * Generates a yearly report with proper structure.
     */
    public IncomeExpenseSummary getYearlyReport(Long userId, int year) {
        try {
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);

            IncomeExpenseSummary summary = getIncomeExpenseSummary(userId, startDate, endDate);
            summary.setYear(year);

            return summary;
        } catch (Exception e) {
            System.err.println("Error in getYearlyReport: " + e.getMessage());
            e.printStackTrace();
            // Return empty yearly report
            IncomeExpenseSummary emptySummary = new IncomeExpenseSummary(new HashMap<>(), new HashMap<>(), BigDecimal.ZERO);
            emptySummary.setYear(year);
            return emptySummary;
        }
    }

    /**
     * Generates a spending analysis report by category for a given date range.
     */
    public List<CategorySpendingReport> getSpendingAnalysisByCategory(Long userId, LocalDate startDate, LocalDate endDate) {
        try {
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
        } catch (Exception e) {
            System.err.println("Error in getSpendingAnalysisByCategory: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // Return empty list instead of throwing
        }
    }
}