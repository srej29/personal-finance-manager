package com.finance.report.dto;

import com.finance.category.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySpendingReport {
    private String categoryName;  // Changed from "name" to "categoryName"
    private CategoryType categoryType;
    private BigDecimal totalAmount;
}