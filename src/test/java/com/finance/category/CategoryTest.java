package com.finance.category;

import com.finance.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Category Entity Tests")
class CategoryTest {

    private Category category;
    private User testUser;

    @BeforeEach
    void setUp() {
        category = new Category();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test@example.com");
        testUser.setFullName("Test User");
        testUser.setPhoneNumber("+1234567890");
    }

    @Test
    @DisplayName("Should create category with all args constructor")
    void shouldCreateCategoryWithAllArgsConstructor() {
        // Given
        Long id = 1L;
        String name = "Custom Food";
        CategoryType type = CategoryType.EXPENSE;
        boolean isCustom = true;

        // When
        Category category = new Category(id, name, type, isCustom, testUser);

        // Then
        assertAll(
                () -> assertEquals(id, category.getId()),
                () -> assertEquals(name, category.getName()),
                () -> assertEquals(type, category.getType()),
                () -> assertTrue(category.isCustom()),
                () -> assertEquals(testUser, category.getUser())
        );
    }

    @Test
    @DisplayName("Should create category with custom constructor")
    void shouldCreateCategoryWithCustomConstructor() {
        // Given
        String name = "Side Business";
        CategoryType type = CategoryType.INCOME;
        boolean isCustom = true;

        // When
        Category category = new Category(name, type, isCustom, testUser);

        // Then
        assertAll(
                () -> assertNull(category.getId()),
                () -> assertEquals(name, category.getName()),
                () -> assertEquals(type, category.getType()),
                () -> assertTrue(category.isCustom()),
                () -> assertEquals(testUser, category.getUser())
        );
    }

    @Test
    @DisplayName("Should create category with no args constructor")
    void shouldCreateCategoryWithNoArgsConstructor() {
        // When
        Category category = new Category();

        // Then
        assertNotNull(category);
        assertNull(category.getId());
        assertNull(category.getName());
        assertNull(category.getType());
        assertFalse(category.isCustom()); // boolean defaults to false
        assertNull(category.getUser());
    }

    @Test
    @DisplayName("Should set and get name")
    void shouldSetAndGetName() {
        // Given
        String name = "Transportation";

        // When
        category.setName(name);

        // Then
        assertEquals(name, category.getName());
    }

    @Test
    @DisplayName("Should set and get category type")
    void shouldSetAndGetCategoryType() {
        // Given
        CategoryType type = CategoryType.INCOME;

        // When
        category.setType(type);

        // Then
        assertEquals(type, category.getType());
    }

    @Test
    @DisplayName("Should set and get custom flag")
    void shouldSetAndGetCustomFlag() {
        // When
        category.setCustom(true);

        // Then
        assertTrue(category.isCustom());

        // When
        category.setCustom(false);

        // Then
        assertFalse(category.isCustom());
    }

    @Test
    @DisplayName("Should set and get user")
    void shouldSetAndGetUser() {
        // When
        category.setUser(testUser);

        // Then
        assertEquals(testUser, category.getUser());
    }

    @Test
    @DisplayName("Should handle null user for default categories")
    void shouldHandleNullUserForDefaultCategories() {
        // When
        category.setUser(null);
        category.setCustom(false);

        // Then
        assertNull(category.getUser());
        assertFalse(category.isCustom());
    }

    @Test
    @DisplayName("Should create default income category")
    void shouldCreateDefaultIncomeCategory() {
        // Given
        Category category = new Category("Salary", CategoryType.INCOME, false, null);

        // Then
        assertAll(
                () -> assertEquals("Salary", category.getName()),
                () -> assertEquals(CategoryType.INCOME, category.getType()),
                () -> assertFalse(category.isCustom()),
                () -> assertNull(category.getUser())
        );
    }

    @Test
    @DisplayName("Should create default expense category")
    void shouldCreateDefaultExpenseCategory() {
        // Given
        Category category = new Category("Food", CategoryType.EXPENSE, false, null);

        // Then
        assertAll(
                () -> assertEquals("Food", category.getName()),
                () -> assertEquals(CategoryType.EXPENSE, category.getType()),
                () -> assertFalse(category.isCustom()),
                () -> assertNull(category.getUser())
        );
    }

    @Test
    @DisplayName("Should create custom category with user")
    void shouldCreateCustomCategoryWithUser() {
        // Given
        Category category = new Category("Gaming", CategoryType.EXPENSE, true, testUser);

        // Then
        assertAll(
                () -> assertEquals("Gaming", category.getName()),
                () -> assertEquals(CategoryType.EXPENSE, category.getType()),
                () -> assertTrue(category.isCustom()),
                () -> assertEquals(testUser, category.getUser())
        );
    }

    @Test
    @DisplayName("Should handle equals and hashCode correctly")
    void shouldHandleEqualsAndHashCodeCorrectly() {
        // Given
        Category category1 = new Category(1L, "Food", CategoryType.EXPENSE, false, null);
        Category category2 = new Category(1L, "Food", CategoryType.EXPENSE, false, null);
        Category category3 = new Category(2L, "Rent", CategoryType.EXPENSE, false, null);

        // Then
        assertEquals(category1, category2);
        assertNotEquals(category1, category3);
        assertEquals(category1.hashCode(), category2.hashCode());
    }

    @Test
    @DisplayName("Should generate meaningful toString")
    void shouldGenerateMeaningfulToString() {
        // Given
        category.setId(1L);
        category.setName("Entertainment");
        category.setType(CategoryType.EXPENSE);
        category.setCustom(true);
        category.setUser(testUser);

        // When
        String toString = category.toString();

        // Then
        assertAll(
                () -> assertTrue(toString.contains("id=1")),
                () -> assertTrue(toString.contains("name=Entertainment")),
                () -> assertTrue(toString.contains("type=EXPENSE")),
                () -> assertTrue(toString.contains("isCustom=true"))
        );
    }

    @Test
    @DisplayName("Should handle CategoryType enum values")
    void shouldHandleCategoryTypeEnumValues() {
        // Test INCOME
        category.setType(CategoryType.INCOME);
        assertEquals(CategoryType.INCOME, category.getType());
        assertEquals("INCOME", category.getType().toString());

        // Test EXPENSE
        category.setType(CategoryType.EXPENSE);
        assertEquals(CategoryType.EXPENSE, category.getType());
        assertEquals("EXPENSE", category.getType().toString());
    }

    @Test
    @DisplayName("Should not be equal to null")
    void shouldNotBeEqualToNull() {
        // Given
        Category category = new Category(1L, "Food", CategoryType.EXPENSE, false, null);

        // Then
        assertNotEquals(category, null);
    }

    @Test
    @DisplayName("Should not be equal to different class")
    void shouldNotBeEqualToDifferentClass() {
        // Given
        Category category = new Category(1L, "Food", CategoryType.EXPENSE, false, null);
        String notACategory = "I'm not a category";

        // Then
        assertNotEquals(category, notACategory);
    }

    @Test
    @DisplayName("Should handle id setting and getting")
    void shouldHandleIdSettingAndGetting() {
        // Given
        Long id = 42L;

        // When
        category.setId(id);

        // Then
        assertEquals(id, category.getId());
    }
}