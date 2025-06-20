package com.finance.goal;

import com.finance.exception.ResourceNotFoundException;
import com.finance.transaction.TransactionRepository;
import com.finance.transaction.Transaction;
import com.finance.user.User;
import com.finance.user.UserService;
import com.finance.category.CategoryType;
import com.finance.goal.dto.GoalRequest;
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
    private final TransactionRepository transactionRepository;

    public GoalService(GoalRepository goalRepository, UserService userService, TransactionRepository transactionRepository) {
        this.goalRepository = goalRepository;
        this.userService = userService;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Calculates the current progress for a goal based on transactions since start date.
     * Progress = (Total Income - Total Expenses) since goal start date
     * This method is crucial for the tests to pass.
     */
    private BigDecimal calculateGoalProgress(User user, LocalDate startDate) {
        System.out.println("=== CALCULATION DEBUG ===");
        System.out.println("User: " + user.getId() + ", Start Date: " + startDate);

        List<Transaction> allTransactions = transactionRepository.findByUserAndDateBetweenOrderByDateDesc(
                user, startDate, LocalDate.now()
        );

        System.out.println("Found " + allTransactions.size() + " transactions:");
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction t : allTransactions) {
            System.out.println("- " + t.getDate() + ": " + t.getCategory().getName() +
                    " (" + t.getCategory().getType() + ") = " + t.getAmount());

            if (t.getCategory().getType() == CategoryType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());
            } else {
                totalExpenses = totalExpenses.add(t.getAmount());
            }
        }

        BigDecimal result = totalIncome.subtract(totalExpenses);
        System.out.println("Income: " + totalIncome + ", Expenses: " + totalExpenses + ", Net: " + result);
        System.out.println("========================");

        return result;
    }

    private void updateGoalProgress(Goal goal) {
        BigDecimal currentProgress = calculateGoalProgress(goal.getUser(), goal.getStartDate());
        goal.setCurrentProgress(currentProgress);
    }

    /**
     * Creates a new savings goal for a user with real-time progress calculation.
     */
    @Transactional
    public Goal createGoal(GoalRequest request, Long userId) {
        // Add validation
        if (request.getGoalName() == null || request.getGoalName().trim().isEmpty()) {
            throw new IllegalArgumentException("Goal name cannot be empty");
        }
        if (request.getTargetAmount() == null || request.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Target amount must be a positive decimal value");
        }
        if (request.getTargetDate() == null || request.getTargetDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Target date must be in the future");
        }

        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Goal goal = new Goal();
        goal.setGoalName(request.getGoalName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        goal.setUser(user);

        if (request.getStartDate() == null) {
            goal.setStartDate(LocalDate.now());
        } else {
            goal.setStartDate(request.getStartDate());

            // CRITICAL: Add validation for start date after target date
            if (request.getStartDate().isAfter(request.getTargetDate())) {
                throw new IllegalArgumentException("Start date cannot be after target date");
            }
        }

        updateGoalProgress(goal);
        return goalRepository.save(goal);
    }


    /**
     * Retrieves a specific goal by its ID for a given user with FRESH progress calculation.
     */
    public Optional<Goal> getGoalById(Long goalId, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Optional<Goal> goalOptional = goalRepository.findByIdAndUser(goalId, user);

        if (goalOptional.isPresent()) {
            Goal goal = goalOptional.get();
            // ALWAYS recalculate progress when retrieving a goal
            BigDecimal currentProgress = calculateGoalProgress(goal.getUser(), goal.getStartDate());
            goal.setCurrentProgress(currentProgress);

            // Also save the updated progress to database
            goal = goalRepository.save(goal);

            return Optional.of(goal);
        }

        return Optional.empty();
    }

    /**
     * Retrieves all goals for a specific user with FRESH progress calculations.
     */
    public List<Goal> getAllGoalsForUser(Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        List<Goal> goals = goalRepository.findByUser(user);

        // Update progress for all goals and save them
        for (Goal goal : goals) {
            BigDecimal currentProgress = calculateGoalProgress(goal.getUser(), goal.getStartDate());
            goal.setCurrentProgress(currentProgress);
            goalRepository.save(goal); // Persist the updated progress
        }

        return goals;
    }

    /**
     * Updates an existing savings goal for a user.
     */
    @Transactional
    public Goal updateGoal(Long goalId, String goalName, BigDecimal targetAmount, LocalDate targetDate, BigDecimal currentProgress, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Goal goal = goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + goalId + " for user: " + userId));

        // Update goal name if provided
        if (goalName != null && !goalName.trim().isEmpty()) {
            goal.setGoalName(goalName);
        }

        // Update target amount if provided
        if (targetAmount != null) {
            if (targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Target amount must be a positive decimal value.");
            }
            goal.setTargetAmount(targetAmount);
        }

        // Update target date if provided
        if (targetDate != null) {
            // Validate target date is not in the past
            if (targetDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Target date cannot be in the past.");
            }

            // Validate start date vs new target date
            if (goal.getStartDate().isAfter(targetDate)) {
                throw new IllegalArgumentException("New target date cannot be before the start date.");
            }
            goal.setTargetDate(targetDate);
        }

        // Always recalculate progress based on current transactions (ignore currentProgress parameter)
        BigDecimal calculatedProgress = calculateGoalProgress(goal.getUser(), goal.getStartDate());
        goal.setCurrentProgress(calculatedProgress);

        return goalRepository.save(goal);
    }

    /**
     * Deletes a goal for a user.
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