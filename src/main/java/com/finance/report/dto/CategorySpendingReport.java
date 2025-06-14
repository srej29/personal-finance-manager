package com.finance.report.dto;

import com.finance.category.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data // Lombok annotation for getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok annotation for no-arg constructor
@AllArgsConstructor // Lombok annotation for all-arg constructor
public class CategorySpendingReport {
    private String categoryName;
    private CategoryType categoryType;
    private BigDecimal totalAmount;
}
