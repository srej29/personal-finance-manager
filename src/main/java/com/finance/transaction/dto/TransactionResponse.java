package com.finance.transaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.finance.category.CategoryType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private LocalDate date;

    // Map internal categoryName to external "category" field
    @JsonProperty("category")
    private String categoryName;

    private CategoryType type;
    private String description;

    // Manual constructor - removed @AllArgsConstructor to avoid conflict
    public TransactionResponse(Long id, BigDecimal amount, LocalDate date,
                               String categoryName, CategoryType type, String description) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.categoryName = categoryName;
        this.type = type;
        this.description = description;
    }
}
