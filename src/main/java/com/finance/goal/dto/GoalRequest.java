package com.finance.goal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GoalRequest {
    private String goalName;  // Remove @NotBlank for updates

    @DecimalMin(value = "0.01", message = "Target amount must be a positive decimal value")
    private BigDecimal targetAmount;  // Remove @NotNull for updates

    @Future(message = "Target date must be in the future")
    private LocalDate targetDate;  // Remove @NotNull for updates

    private LocalDate startDate;  // Optional
}