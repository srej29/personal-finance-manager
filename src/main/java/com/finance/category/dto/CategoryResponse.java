
// CategoryResponse.java
package com.finance.category.dto;

import com.finance.category.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private String name;
    private CategoryType type;
    private boolean custom; // Note: "custom" not "isCustom" to match test expectations
}