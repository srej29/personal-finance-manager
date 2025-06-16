package com.finance.report.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
public class IncomeExpenseSummary {
    private Integer month;          // For monthly reports
    private Integer year;           // For monthly and yearly reports
    private Map<String, BigDecimal> totalIncome;    // Category name -> amount
    private Map<String, BigDecimal> totalExpenses;  // Category name -> amount
    private BigDecimal netSavings;  // Total income - total expenses

    // Constructor for monthly reports
    public IncomeExpenseSummary(Integer month, Integer year, Map<String, BigDecimal> totalIncome,
                                Map<String, BigDecimal> totalExpenses, BigDecimal netSavings) {
        this.month = month;
        this.year = year;
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.netSavings = netSavings;
    }

    // Constructor for yearly reports
    public IncomeExpenseSummary(Integer year, Map<String, BigDecimal> totalIncome,
                                Map<String, BigDecimal> totalExpenses, BigDecimal netSavings) {
        this.year = year;
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.netSavings = netSavings;
    }

    // Constructor for basic summary (without month/year)
    public IncomeExpenseSummary(Map<String, BigDecimal> totalIncome,
                                Map<String, BigDecimal> totalExpenses, BigDecimal netSavings) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.netSavings = netSavings;
    }
}