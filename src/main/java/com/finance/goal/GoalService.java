package com.finance.goal;

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
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserService userService;

    public GoalService(GoalRepository goalRepository, UserService userService) {
        this.goalRepository = goalRepository;
        this.userService = userService;
    }

    /**
     * Creates a new savings goal for a user.
     * @param goalName The name of the goal.
     * @param targetAmount The target amount for the goal.
     * @param targetDate The target date to achieve the goal.
     * @param startDate The start date of the goal.
     * @param initialProgress The initial current progress (can be 0 or null).
     * @param userId The ID of the user creating the goal.
     * @return The created Goal object.
     * @throws IllegalArgumentException if validation rules are not met.
     * @throws ResourceNotFoundException if the user is not found.
     */
    @Transactional
    public Goal createGoal(String goalName, BigDecimal targetAmount, LocalDate targetDate, LocalDate startDate, BigDecimal initialProgress, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (startDate.isAfter(targetDate)) {
            throw new IllegalArgumentException("Start date cannot be after target date.");
        }

        Goal goal = new Goal();
        goal.setGoalName(goalName);
        goal.setTargetAmount(targetAmount);
        goal.setTargetDate(targetDate);
        goal.setStartDate(startDate);
        goal.setCurrentProgress(initialProgress != null ? initialProgress : BigDecimal.ZERO); // Set initial progress
        goal.setUser(user);

        return goalRepository.save(goal);
    }

    /**
     * Retrieves a specific goal by its ID for a given user.
     * Ensures data isolation.
     * @param goalId The ID of the goal.
     * @param userId The ID of the user.
     * @return An Optional containing the goal if found and owned by the user, or empty.
     */
    public Optional<Goal> getGoalById(Long goalId, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return goalRepository.findByIdAndUser(goalId, user);
    }

    /**
     * Retrieves all goals for a specific user.
     * @param userId The ID of the user.
     * @return A list of goals.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public List<Goal> getAllGoalsForUser(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return goalRepository.findByUser(user);
    }

    /**
     * Updates an existing savings goal for a user.
     * Allows updating goalName, targetAmount, targetDate, and currentProgress.
     * @param goalId The ID of the goal to update.
     * @param goalName The new goal name (optional).
     * @param targetAmount The new target amount (optional).
     * @param targetDate The new target date (optional).
     * @param currentProgress The new current progress (optional).
     * @param userId The ID of the user performing the update.
     * @return The updated Goal object.
     * @throws ResourceNotFoundException if the goal or user is not found.
     * @throws IllegalArgumentException if validation rules are not met (e.g., targetAmount non-positive).
     */
    @Transactional
    public Goal updateGoal(Long goalId, String goalName, BigDecimal targetAmount, LocalDate targetDate, BigDecimal currentProgress, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Goal goal = goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + goalId + " for user: " + userId));

        if (goalName != null && !goalName.isEmpty()) {
            goal.setGoalName(goalName);
        }

        if (targetAmount != null) {
            if (targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Target amount must be a positive decimal value.");
            }
            goal.setTargetAmount(targetAmount);
        }

        if (targetDate != null) {
            if (goal.getStartDate().isAfter(targetDate)) { // Re-validate start vs target date
                throw new IllegalArgumentException("New target date cannot be before the start date.");
            }
            goal.setTargetDate(targetDate);
        }

        if (currentProgress != null) {
            if (currentProgress.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Current progress cannot be negative.");
            }
            // Ensure current progress does not exceed target amount if it's set as a hard limit
            // Optional: currentProgress = currentProgress.min(goal.getTargetAmount());
            goal.setCurrentProgress(currentProgress);
        }

        return goalRepository.save(goal);
    }

    /**
     * Deletes a goal for a user.
     * Ensures data isolation.
     * @param goalId The ID of the goal to delete.
     * @param userId The ID of the user performing the deletion.
     * @throws ResourceNotFoundException if the goal or user is not found.
     */
    @Transactional
    public void deleteGoal(Long goalId, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Goal goal = goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + goalId + " for user: " + userId));

        goalRepository.delete(goal);
    }
}
