package com.finance.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data // Lombok annotation for getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok annotation for no-arg constructor
@AllArgsConstructor // Lombok annotation for all-arg constructor
public class IncomeExpenseSummary {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance; // totalIncome - totalExpenses
}
