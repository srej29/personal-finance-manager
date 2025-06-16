package com.finance.transaction.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {

    @DecimalMin(value = "0.01", message = "Amount must be a positive decimal value")
    private BigDecimal amount;  // Remove @NotNull for updates

    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDate date;  // Remove @NotNull for updates

    private String categoryName;  // Remove @NotBlank for updates

    private String description;

    // Handle both "category" and "categoryName" from JSON
    @JsonSetter("category")
    public void setCategory(String category) {
        this.categoryName = category;
    }
}