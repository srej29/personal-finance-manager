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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

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
     * @return The ID of the authenticated user.
     * @throws IllegalStateException if no user is authenticated or user not found.
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
     * Generates an income vs. expense summary for a specified date range for the authenticated user.
     * @param startDate The start date of the report period (format: YYYY-MM-DD).
     * @param endDate The end date of the report period (format: YYYY-MM-DD).
     * @return An IncomeExpenseSummary DTO.
     */
    @GetMapping("/summary")
    public ResponseEntity<IncomeExpenseSummary> getIncomeExpenseSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = getAuthenticatedUserId();
        IncomeExpenseSummary summary = reportService.getIncomeExpenseSummary(userId, startDate, endDate);
        return new ResponseEntity<>(summary, HttpStatus.OK);
    }

    /**
     * Generates a spending analysis by category for a specified date range for the authenticated user.
     * @param startDate The start date of the report period (format: YYYY-MM-DD).
     * @param endDate The end date of the report period (format: YYYY-MM-DD).
     * @return A list of CategorySpendingReport DTOs.
     */
    @GetMapping("/spending-by-category")
    public ResponseEntity<List<CategorySpendingReport>> getSpendingAnalysisByCategory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = getAuthenticatedUserId();
        List<CategorySpendingReport> report = reportService.getSpendingAnalysisByCategory(userId, startDate, endDate);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    // Goal progress reports are implicitly available via GET /api/goals or GET /api/goals/{id}
    // and the GoalResponse DTO's calculated fields.
}
