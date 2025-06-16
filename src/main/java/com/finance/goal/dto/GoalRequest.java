package com.finance.goal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GoalRequest {
    @NotBlank(message = "Goal name cannot be empty")  // ADD THIS
    private String goalName;

    @NotNull(message = "Target amount cannot be null")  // ADD THIS
    @DecimalMin(value = "0.01", message = "Target amount must be a positive decimal value")
    private BigDecimal targetAmount;

    @NotNull(message = "Target date cannot be null")  // ADD THIS
    @Future(message = "Target date must be in the future")  // Fixed message
    private LocalDate targetDate;

    // REMOVE @NotNull - allow null for default behavior
    private LocalDate startDate;  // Optional - defaults to current date if null

    // REMOVE this field - it shouldn't be in request DTO
    // @DecimalMin(value = "0.00", message = "Current progress cannot be negative")
    // private BigDecimal currentProgress;
}