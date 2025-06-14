package com.finance.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data // Lombok annotation for getters, setters, toString, equals, hashCode
public class TransactionRequest {

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be a positive decimal value")
    private BigDecimal amount;

    @NotNull(message = "Date cannot be null")
    @PastOrPresent(message = "Transaction date cannot be in the future") // Date validation as per assignment
    private LocalDate date;

    @NotBlank(message = "Category name cannot be empty")
    private String categoryName;

    private String description; // Optional
}
