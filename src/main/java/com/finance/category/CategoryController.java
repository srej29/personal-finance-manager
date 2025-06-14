package com.finance.category;

import com.finance.category.dto.CategoryRequest;
import com.finance.category.dto.CategoryResponse;
import com.finance.user.User;
import com.finance.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService; // To get the authenticated user's ID

    public CategoryController(CategoryService categoryService, UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    /**
     * Helper method to get the authenticated user's ID.
     * This relies on Spring Security and the UserDetailsService implementation.
     * @return The ID of the authenticated user.
     * @throws IllegalStateException if no user is authenticated.
     */
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User not authenticated."); // This should be caught by Spring Security's filter chain (401)
        }
        // Assuming the principal is an instance of org.springframework.security.core.userdetails.User
        String username = authentication.getName();
        return userService.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + username))
                .getId();
    }

    /**
     * Retrieves all categories accessible to the authenticated user.
     * Includes both default and custom categories created by the user.
     * @return A list of CategoryResponse DTOs.
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        Long userId = getAuthenticatedUserId();
        List<Category> categories = categoryService.getAllCategoriesForUser(userId);
        List<CategoryResponse> responses = categories.stream()
                .map(category -> new CategoryResponse(category.getName(), category.getType(), category.isCustom()))
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * Creates a new custom category for the authenticated user.
     * @param request The CategoryRequest DTO containing name and type.
     * @return The created CategoryResponse DTO.
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCustomCategory(@Valid @RequestBody CategoryRequest request) {
        Long userId = getAuthenticatedUserId();
        Category newCategory = categoryService.createCustomCategory(request.getName(), request.getType(), userId);
        CategoryResponse response = new CategoryResponse(newCategory.getName(), newCategory.getType(), newCategory.isCustom());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Deletes a custom category for the authenticated user by its name.
     * Default categories cannot be deleted via this endpoint.
     * @param name The name of the custom category to delete.
     * @return A success message.
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<String> deleteCustomCategory(@PathVariable String name) {
        Long userId = getAuthenticatedUserId();
        categoryService.deleteCustomCategory(name, userId);
        return new ResponseEntity<>("Category deleted successfully", HttpStatus.OK);
    }
}
