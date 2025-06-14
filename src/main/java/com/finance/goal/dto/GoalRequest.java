package com.finance.goal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data // Lombok annotation for getters, setters, toString, equals, hashCode
public class GoalRequest {

    // For CREATE
    @NotBlank(message = "Goal name cannot be empty")
    private String goalName;

    @NotNull(message = "Target amount cannot be null")
    @DecimalMin(value = "0.01", message = "Target amount must be a positive decimal value")
    private BigDecimal targetAmount;

    @NotNull(message = "Target date cannot be null")
    @FutureOrPresent(message = "Target date cannot be in the past")
    private LocalDate targetDate;

    @NotNull(message = "Start date cannot be null")
    @PastOrPresent(message = "Start date cannot be in the future")
    private LocalDate startDate;

    // For UPDATES and optional for CREATE
    @DecimalMin(value = "0.00", message = "Current progress cannot be negative")
    private BigDecimal currentProgress; // Optional for create, can be null or 0.00.
    // For update, allows adjusting current progress.
}
