package com.finance.goal.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GoalRequest {
    // REMOVE ALL validation annotations for partial updates
    private String goalName;
    private BigDecimal targetAmount;
    private LocalDate targetDate;
    private LocalDate startDate;
}