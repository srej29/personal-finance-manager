package com.finance.report;

import com.finance.exception.ResourceNotFoundException;
import com.finance.report.dto.CategorySpendingReport;
import com.finance.report.dto.IncomeExpenseSummary;
import com.finance.user.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    public ReportController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    /**
     * Helper method to get the authenticated user's ID.
     */
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User not authenticated.");
        }
        String username = authentication.getName();
        return userService.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + username))
                .getId();
    }

    /**
     * Generates an income vs. expense summary for a specified date range.
     */
    @GetMapping("/summary")
    public ResponseEntity<IncomeExpenseSummary> getIncomeExpenseSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Long userId = getAuthenticatedUserId();
            IncomeExpenseSummary summary = reportService.getIncomeExpenseSummary(userId, startDate, endDate);
            return new ResponseEntity<>(summary, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error in getIncomeExpenseSummary: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Generates a spending analysis by category for a specified date range.
     */
    @GetMapping("/spending-by-category")
    public ResponseEntity<List<CategorySpendingReport>> getSpendingAnalysisByCategory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Long userId = getAuthenticatedUserId();
            List<CategorySpendingReport> report = reportService.getSpendingAnalysisByCategory(userId, startDate, endDate);
            return new ResponseEntity<>(report, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error in getSpendingAnalysisByCategory: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Generates a monthly report for a specific year and month.
     */
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<Map<String, Object>> getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month) {
        try {
            // Validate month
            if (month < 1 || month > 12) {
                return ResponseEntity.badRequest().build();
            }

            Long userId = getAuthenticatedUserId();
            IncomeExpenseSummary summary = reportService.getMonthlyReport(userId, year, month);

            // Convert to the format tests expect
            Map<String, Object> response = new HashMap<>();
            response.put("month", month);
            response.put("year", year);
            response.put("totalIncome", summary.getTotalIncome());
            response.put("totalExpenses", summary.getTotalExpenses());
            response.put("netSavings", summary.getNetSavings());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error in getMonthlyReport: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<Map<String, Object>> getYearlyReport(@PathVariable int year) {
        try {
            Long userId = getAuthenticatedUserId();
            IncomeExpenseSummary summary = reportService.getYearlyReport(userId, year);

            // Convert to the format tests expect
            Map<String, Object> response = new HashMap<>();
            response.put("year", year);
            response.put("totalIncome", summary.getTotalIncome());
            response.put("totalExpenses", summary.getTotalExpenses());
            response.put("netSavings", summary.getNetSavings());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error in getYearlyReport: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}