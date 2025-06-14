package com.finance.transaction;

import com.finance.category.Category;
import com.finance.category.CategoryType;
import com.finance.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@DisplayName("Transaction Entity Tests")
class TransactionTest {

    private Transaction transaction;
    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test@example.com");
        testUser.setFullName("Test User");
        testUser.setPhoneNumber("+1234567890");

        // Create test category
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Salary");
        testCategory.setType(CategoryType.INCOME);
        testCategory.setCustom(false);
    }

    @Test
    @DisplayName("Should create transaction with all args constructor")
    void shouldCreateTransactionWithAllArgsConstructor() {
        // Given
        Long id = 1L;
        BigDecimal amount = new BigDecimal("1500.50");
        LocalDate date = LocalDate.of(2024, 1, 15);
        String description = "Monthly salary";

        // When
        Transaction transaction = new Transaction(id, amount, date, testCategory, testUser, description);

        // Then
        assertAll(
                () -> assertEquals(id, transaction.getId()),
                () -> assertEquals(amount, transaction.getAmount()),
                () -> assertEquals(date, transaction.getDate()),
                () -> assertEquals(testCategory, transaction.getCategory()),
                () -> assertEquals(testUser, transaction.getUser()),
                () -> assertEquals(description, transaction.getDescription())
        );
    }

    @Test
    @DisplayName("Should create transaction with no args constructor")
    void shouldCreateTransactionWithNoArgsConstructor() {
        // When
        Transaction transaction = new Transaction();

        // Then
        assertNotNull(transaction);
        assertNull(transaction.getId());
        assertNull(transaction.getAmount());
        assertNull(transaction.getDate());
        assertNull(transaction.getCategory());
        assertNull(transaction.getUser());
        assertNull(transaction.getDescription());
    }

    @Test
    @DisplayName("Should set and get amount")
    void shouldSetAndGetAmount() {
        // Given
        BigDecimal amount = new BigDecimal("2500.75");

        // When
        transaction.setAmount(amount);

        // Then
        assertEquals(amount, transaction.getAmount());
    }

    @Test
    @DisplayName("Should set and get date")
    void shouldSetAndGetDate() {
        // Given
        LocalDate date = LocalDate.of(2024, 2, 20);

        // When
        transaction.setDate(date);

        // Then
        assertEquals(date, transaction.getDate());
    }

    @Test
    @DisplayName("Should set and get category")
    void shouldSetAndGetCategory() {
        // When
        transaction.setCategory(testCategory);

        // Then
        assertEquals(testCategory, transaction.getCategory());
    }

    @Test
    @DisplayName("Should set and get user")
    void shouldSetAndGetUser() {
        // When
        transaction.setUser(testUser);

        // Then
        assertEquals(testUser, transaction.getUser());
    }

    @Test
    @DisplayName("Should set and get description")
    void shouldSetAndGetDescription() {
        // Given
        String description = "Grocery shopping at supermarket";

        // When
        transaction.setDescription(description);

        // Then
        assertEquals(description, transaction.getDescription());
    }

    @Test
    @DisplayName("Should handle null description")
    void shouldHandleNullDescription() {
        // When
        transaction.setDescription(null);

        // Then
        assertNull(transaction.getDescription());
    }

    @Test
    @DisplayName("Should handle empty description")
    void shouldHandleEmptyDescription() {
        // Given
        String description = "";

        // When
        transaction.setDescription(description);

        // Then
        assertEquals(description, transaction.getDescription());
    }

    @Test
    @DisplayName("Should set and get id")
    void shouldSetAndGetId() {
        // Given
        Long id = 42L;

        // When
        transaction.setId(id);

        // Then
        assertEquals(id, transaction.getId());
    }

    @Test
    @DisplayName("Should handle equals and hashCode correctly")
    void shouldHandleEqualsAndHashCodeCorrectly() {
        // Given
        BigDecimal amount = new BigDecimal("1000.00");
        LocalDate date = LocalDate.of(2024, 1, 1);
        String description = "Test transaction";

        Transaction transaction1 = new Transaction(1L, amount, date, testCategory, testUser, description);
        Transaction transaction2 = new Transaction(1L, amount, date, testCategory, testUser, description);
        Transaction transaction3 = new Transaction(2L, new BigDecimal("2000.00"), date, testCategory, testUser, "Different");

        // Then
        assertEquals(transaction1, transaction2);
        assertNotEquals(transaction1, transaction3);
        assertEquals(transaction1.hashCode(), transaction2.hashCode());
    }

    @Test
    @DisplayName("Should generate meaningful toString")
    void shouldGenerateMeaningfulToString() {
        // Given
        transaction.setId(1L);
        transaction.setAmount(new BigDecimal("500.00"));
        transaction.setDate(LocalDate.of(2024, 1, 15));
        transaction.setDescription("Test description");
        transaction.setCategory(testCategory);
        transaction.setUser(testUser);

        // When
        String toString = transaction.toString();

        // Then
        assertAll(
                () -> assertTrue(toString.contains("id=1")),
                () -> assertTrue(toString.contains("amount=500.00")),
                () -> assertTrue(toString.contains("2024-01-15")),
                () -> assertTrue(toString.contains("Test description"))
        );
    }

    @Test
    @DisplayName("Should handle BigDecimal precision correctly")
    void shouldHandleBigDecimalPrecisionCorrectly() {
        // Given
        BigDecimal amount1 = new BigDecimal("100.50");
        BigDecimal amount2 = new BigDecimal("100.5");

        // When
        transaction.setAmount(amount1);

        // Then
        assertEquals(0, transaction.getAmount().compareTo(amount2));
    }

    @Test
    @DisplayName("Should not be equal to null")
    void shouldNotBeEqualToNull() {
        // Given
        Transaction transaction = new Transaction(1L, new BigDecimal("100"), LocalDate.now(), testCategory, testUser, "test");

        // Then
        assertNotEquals(transaction, null);
    }

    @Test
    @DisplayName("Should not be equal to different class")
    void shouldNotBeEqualToDifferentClass() {
        // Given
        Transaction transaction = new Transaction(1L, new BigDecimal("100"), LocalDate.now(), testCategory, testUser, "test");
        String notATransaction = "I'm not a transaction";

        // Then
        assertNotEquals(transaction, notATransaction);
    }
}