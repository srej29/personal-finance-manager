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
     * Converts a Goal entity to a GoalResponse DTO, calculating derived fields.
     * @param goal The Goal entity.
     * @return The GoalResponse DTO.
     */
    private GoalResponse convertToDto(Goal goal) {
        BigDecimal remainingAmount = goal.getTargetAmount().subtract(goal.getCurrentProgress());
        remainingAmount = remainingAmount.compareTo(BigDecimal.ZERO) > 0 ? remainingAmount : BigDecimal.ZERO; // Ensure not negative

        BigDecimal progressPercentage = BigDecimal.ZERO;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = goal.getCurrentProgress()
                    .divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP) // Calculate with precision
                    .multiply(new BigDecimal(100))
                    .setScale(2, RoundingMode.HALF_UP); // Round to 2 decimal places for percentage
        }
        progressPercentage = progressPercentage.min(new BigDecimal(100)); // Cap at 100%

        return new GoalResponse(
                goal.getId(),
                goal.getGoalName(),
                goal.getTargetAmount(),
                goal.getTargetDate(),
                goal.getStartDate(),
                goal.getCurrentProgress(),
                progressPercentage,
                remainingAmount
        );
    }

    /**
     * Creates a new savings goal for the authenticated user.
     * @param request The GoalRequest DTO containing goal details.
     * @return The created GoalResponse DTO.
     */
    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalRequest request) {
        Long userId = getAuthenticatedUserId();
        Goal newGoal = goalService.createGoal(
                request.getGoalName(),
                request.getTargetAmount(),
                request.getTargetDate(),
                request.getStartDate(),
                request.getCurrentProgress(), // Allows initial progress to be set
                userId
        );
        return new ResponseEntity<>(convertToDto(newGoal), HttpStatus.CREATED);
    }

    /**
     * Retrieves all savings goals for the authenticated user.
     * @return A list of GoalResponse DTOs.
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
     * @param id The ID of the goal.
     * @return The GoalResponse DTO.
     */
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoalById(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        Goal goal = goalService.getGoalById(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + id));
        return new ResponseEntity<>(convertToDto(goal), HttpStatus.OK);
    }

    /**
     * Updates an existing savings goal for the authenticated user.
     * @param id The ID of the goal to update.
     * @param request The GoalRequest DTO containing updated details.
     * @return The updated GoalResponse DTO.
     */
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(@PathVariable Long id, @Valid @RequestBody GoalRequest request) {
        Long userId = getAuthenticatedUserId();
        Goal updatedGoal = goalService.updateGoal(
                id,
                request.getGoalName(),
                request.getTargetAmount(),
                request.getTargetDate(),
                request.getCurrentProgress(),
                userId
        );
        return new ResponseEntity<>(convertToDto(updatedGoal), HttpStatus.OK);
    }

    /**
     * Deletes a savings goal for the authenticated user.
     * @param id The ID of the goal to delete.
     * @return A success message.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteGoal(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        goalService.deleteGoal(id, userId);
        return new ResponseEntity<>("Goal deleted successfully", HttpStatus.OK);
    }
}
