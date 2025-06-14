package com.finance.category.dto;

import com.finance.category.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok annotation for getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok annotation for no-arg constructor
@AllArgsConstructor // Lombok annotation for all-arg constructor
public class CategoryResponse {
    private String name;
    private CategoryType type;
    private boolean isCustom;
}
