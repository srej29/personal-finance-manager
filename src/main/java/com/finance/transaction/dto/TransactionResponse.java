package com.finance.transaction.dto;

import com.finance.category.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data // Lombok annotation for getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok annotation for no-arg constructor
@AllArgsConstructor // Lombok annotation for all-arg constructor
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private String categoryName;
    private CategoryType categoryType; // Include category type for filtering/reporting
    private String description;
}
