package com.finance.goal;

import com.finance.goal.dto.GoalRequest;
import com.finance.goal.dto.GoalResponse;
import com.finance.exception.ResourceNotFoundException;
import com.finance.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;
    private final UserService userService;

    public GoalController(GoalService goalService, UserService userService) {
        this.goalService = goalService;
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
     * Converts a Goal entity to a GoalResponse DTO, calculating derived fields.
     */
    private GoalResponse convertToDto(Goal goal) {
        BigDecimal currentProgress = goal.getCurrentProgress() != null ? goal.getCurrentProgress() : BigDecimal.ZERO;
        BigDecimal targetAmount = goal.getTargetAmount();

        BigDecimal remainingAmount = targetAmount.subtract(currentProgress);
        remainingAmount = remainingAmount.compareTo(BigDecimal.ZERO) > 0 ? remainingAmount : BigDecimal.ZERO;

        BigDecimal progressPercentage = BigDecimal.ZERO;
        if (targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = currentProgress
                    .divide(targetAmount, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));

            // FINAL CORRECT FIX: Set to 2 decimal places, then strip trailing zeros
            progressPercentage = progressPercentage.setScale(2, RoundingMode.HALF_UP);
            progressPercentage = progressPercentage.stripTrailingZeros();

            // Special handling for zero to avoid scientific notation
            if (progressPercentage.compareTo(BigDecimal.ZERO) == 0) {
                progressPercentage = new BigDecimal("0.0");
            }
            // Ensure we don't get scientific notation for other values
            else if (progressPercentage.scale() < 0) {
                progressPercentage = progressPercentage.setScale(1, RoundingMode.HALF_UP);
            }
        } else {
            progressPercentage = new BigDecimal("0.0");
        }

        // Ensure percentage doesn't exceed 100
        progressPercentage = progressPercentage.min(new BigDecimal("100.0"));

        return new GoalResponse(
                goal.getId(),
                goal.getGoalName(),
                goal.getTargetAmount(),
                goal.getTargetDate(),
                goal.getStartDate(),
                currentProgress,
                progressPercentage,
                remainingAmount
        );
    }

    /**
     * Creates a new savings goal for the authenticated user.
     */
    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalRequest request) {
        Long userId = getAuthenticatedUserId();

        // FIXED: Pass the entire request object and userId
        Goal newGoal = goalService.createGoal(request, userId);
        return new ResponseEntity<>(convertToDto(newGoal), HttpStatus.CREATED);
    }

    /**
     * Retrieves all savings goals for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<GoalResponse>> getAllGoals() {
        Long userId = getAuthenticatedUserId();
        List<Goal> goals = goalService.getAllGoalsForUser(userId);
        List<GoalResponse> responses = goals.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * Retrieves a specific savings goal by ID for the authenticated user.
     */
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoalById(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        Goal goal = goalService.getGoalById(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + id + " for user: " + userId));
        return new ResponseEntity<>(convertToDto(goal), HttpStatus.OK);
    }

    /**
     * Updates an existing savings goal for the authenticated user.
     * Note: currentProgress is calculated automatically, not provided by user.
     */
    @PutMapping("/{id}")

    public ResponseEntity<GoalResponse> updateGoal(@PathVariable Long id, @Valid @RequestBody GoalRequest request) {
        Long userId = getAuthenticatedUserId();

        // FIXED: Add the missing currentProgress parameter (pass null since it will be recalculated)
        Goal updatedGoal = goalService.updateGoal(
                id,
                request.getGoalName(),
                request.getTargetAmount(),
                request.getTargetDate(),
                null, // currentProgress - will be recalculated by service
                userId
        );
        return new ResponseEntity<>(convertToDto(updatedGoal), HttpStatus.OK);
    }

    /**
     * Deletes a savings goal for the authenticated user.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteGoal(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        goalService.deleteGoal(id, userId);
        return new ResponseEntity<>("Goal deleted successfully", HttpStatus.OK);
    }
}