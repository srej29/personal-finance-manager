package com.finance.transaction.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {
    // REMOVE ALL @NotNull, @NotBlank, @DecimalMin annotations for partial updates to work
    private BigDecimal amount;
    private LocalDate date;
    private String categoryName;
    private String description;

    @JsonSetter("category")
    public void setCategory(String category) {
        this.categoryName = category;
    }
}