package com.finance.category.dto;

import com.finance.category.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data // Lombok annotation for getters, setters, toString, equals, hashCode
public class CategoryRequest {

    @NotBlank(message = "Category name cannot be empty")
    private String name;

    @NotNull(message = "Category type cannot be null (INCOME or EXPENSE)")
    private CategoryType type;
}
