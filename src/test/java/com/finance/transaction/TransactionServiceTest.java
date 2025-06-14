package com.finance.transaction;

import com.finance.category.Category;
import com.finance.category.CategoryService;
import com.finance.category.CategoryType;
import com.finance.exception.ResourceNotFoundException;
import com.finance.user.User;
import com.finance.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transaction Service Tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Category testCategory;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test@example.com");
        testUser.setFullName("Test User");
        testUser.setPhoneNumber("+1234567890");

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Salary");
        testCategory.setType(CategoryType.INCOME);
        testCategory.setCustom(false);

        testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setAmount(new BigDecimal("2500.00"));
        testTransaction.setDate(LocalDate.of(2024, 1, 15));
        testTransaction.setCategory(testCategory);
        testTransaction.setUser(testUser);
        testTransaction.setDescription("Monthly salary");
    }

    @Test
    @DisplayName("Should create transaction successfully")
    void shouldCreateTransactionSuccessfully() {
        // Given
        BigDecimal amount = new BigDecimal("1500.00");
        LocalDate date = LocalDate.of(2024, 2, 1);
        String categoryName = "Salary";
        String description = "February salary";
        Long userId = 1L;

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(categoryService.findAccessibleCategoryByName(categoryName, userId))
                .thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        Transaction result = transactionService.createTransaction(amount, date, categoryName, description, userId);

        // Then
        assertNotNull(result);
        assertEquals(testTransaction.getId(), result.getId());
        assertEquals(testTransaction.getAmount(), result.getAmount());
        assertEquals(testTransaction.getCategory(), result.getCategory());
        assertEquals(testTransaction.getUser(), result.getUser());

        verify(userService).findById(userId);
        verify(categoryService).findAccessibleCategoryByName(categoryName, userId);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw exception when amount is negative")
    void shouldThrowExceptionWhenAmountIsNegative() {
        // Given
        BigDecimal negativeAmount = new BigDecimal("-100.00");
        LocalDate date = LocalDate.now();
        String categoryName = "Food";
        String description = "Test";
        Long userId = 1L;

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.createTransaction(negativeAmount, date, categoryName, description, userId)
        );

        assertEquals("Amount must be a positive decimal value.", exception.getMessage());
        verify(userService, never()).findById(any());
        verify(categoryService, never()).findAccessibleCategoryByName(any(), any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when amount is zero")
    void shouldThrowExceptionWhenAmountIsZero() {
        // Given
        BigDecimal zeroAmount = BigDecimal.ZERO;
        LocalDate date = LocalDate.now();
        String categoryName = "Food";
        String description = "Test";
        Long userId = 1L;

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.createTransaction(zeroAmount, date, categoryName, description, userId)
        );

        assertEquals("Amount must be a positive decimal value.", exception.getMessage());
        verify(userService, never()).findById(any());
        verify(categoryService, never()).findAccessibleCategoryByName(any(), any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when date is in future")
    void shouldThrowExceptionWhenDateIsInFuture() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        LocalDate futureDate = LocalDate.now().plusDays(1);
        String categoryName = "Food";
        String description = "Test";
        Long userId = 1L;

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.createTransaction(amount, futureDate, categoryName, description, userId)
        );

        assertEquals("Transaction date cannot be in the future.", exception.getMessage());
        verify(userService, never()).findById(any());
        verify(categoryService, never()).findAccessibleCategoryByName(any(), any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when user not found during transaction creation")
    void shouldThrowExceptionWhenUserNotFoundDuringTransactionCreation() {
        // Given
        BigDecimal amount = new BigDecimal("1500.00");
        LocalDate date = LocalDate.of(2024, 2, 1);
        String categoryName = "Salary";
        String description = "February salary";
        Long userId = 999L;

        when(userService.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.createTransaction(amount, date, categoryName, description, userId)
        );

        assertEquals("User not found with ID: " + userId, exception.getMessage());
        verify(userService).findById(userId);
        verify(categoryService, never()).findAccessibleCategoryByName(any(), any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when category not found during transaction creation")
    void shouldThrowExceptionWhenCategoryNotFoundDuringTransactionCreation() {
        // Given
        BigDecimal amount = new BigDecimal("1500.00");
        LocalDate date = LocalDate.of(2024, 2, 1);
        String categoryName = "NonExistentCategory";
        String description = "Test transaction";
        Long userId = 1L;

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(categoryService.findAccessibleCategoryByName(categoryName, userId))
                .thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.createTransaction(amount, date, categoryName, description, userId)
        );

        assertEquals("Category not found or not accessible: " + categoryName, exception.getMessage());
        verify(userService).findById(userId);
        verify(categoryService).findAccessibleCategoryByName(categoryName, userId);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get transaction by ID successfully")
    void shouldGetTransactionByIdSuccessfully() {
        // Given
        Long transactionId = 1L;
        Long userId = 1L;

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByIdAndUser(transactionId, testUser))
                .thenReturn(Optional.of(testTransaction));

        // When
        Optional<Transaction> result = transactionService.getTransactionById(transactionId, userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testTransaction, result.get());
        verify(userService).findById(userId);
        verify(transactionRepository).findByIdAndUser(transactionId, testUser);
    }

    @Test
    @DisplayName("Should return empty when transaction not found by ID")
    void shouldReturnEmptyWhenTransactionNotFoundById() {
        // Given
        Long transactionId = 999L;
        Long userId = 1L;

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByIdAndUser(transactionId, testUser))
                .thenReturn(Optional.empty());

        // When
        Optional<Transaction> result = transactionService.getTransactionById(transactionId, userId);

        // Then
        assertFalse(result.isPresent());
        verify(userService).findById(userId);
        verify(transactionRepository).findByIdAndUser(transactionId, testUser);
    }

    @Test
    @DisplayName("Should throw exception when user not found during get transaction by ID")
    void shouldThrowExceptionWhenUserNotFoundDuringGetTransactionById() {
        // Given
        Long transactionId = 1L;
        Long userId = 999L;

        when(userService.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.getTransactionById(transactionId, userId)
        );

        assertEquals("User not found with ID: " + userId, exception.getMessage());
        verify(userService).findById(userId);
        verify(transactionRepository, never()).findByIdAndUser(any(), any());
    }

    @Test
    @DisplayName("Should get filtered transactions successfully")
    void shouldGetFilteredTransactionsSuccessfully() {
        // Given
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        String categoryName = "Salary";
        CategoryType categoryType = CategoryType.INCOME;

        List<Transaction> expectedTransactions = Arrays.asList(testTransaction);

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(categoryService.findAccessibleCategoryByName(categoryName, userId))
                .thenReturn(Optional.of(testCategory));
        when(transactionRepository.findFilteredTransactions(testUser, startDate, endDate, categoryName, categoryType))
                .thenReturn(expectedTransactions);

        // When
        List<Transaction> result = transactionService.getFilteredTransactions(
                userId, startDate, endDate, categoryName, categoryType);

        // Then
        assertEquals(expectedTransactions, result);
        verify(userService).findById(userId);
        verify(categoryService).findAccessibleCategoryByName(categoryName, userId);
        verify(transactionRepository).findFilteredTransactions(testUser, startDate, endDate, categoryName, categoryType);
    }

    @Test
    @DisplayName("Should get filtered transactions without category filter")
    void shouldGetFilteredTransactionsWithoutCategoryFilter() {
        // Given
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        List<Transaction> expectedTransactions = Arrays.asList(testTransaction);

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findFilteredTransactions(testUser, startDate, endDate, null, null))
                .thenReturn(expectedTransactions);

        // When
        List<Transaction> result = transactionService.getFilteredTransactions(
                userId, startDate, endDate, null, null);

        // Then
        assertEquals(expectedTransactions, result);
        verify(userService).findById(userId);
        verify(categoryService, never()).findAccessibleCategoryByName(any(), any());
        verify(transactionRepository).findFilteredTransactions(testUser, startDate, endDate, null, null);
    }

    @Test
    @DisplayName("Should update transaction successfully")
    void shouldUpdateTransactionSuccessfully() {
        // Given
        Long transactionId = 1L;
        Long userId = 1L;
        BigDecimal newAmount = new BigDecimal("3000.00");
        String newCategoryName = "Bonus";
        String newDescription = "Year-end bonus";

        Category newCategory = new Category();
        newCategory.setName("Bonus");
        newCategory.setType(CategoryType.INCOME);

        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId(transactionId);
        updatedTransaction.setAmount(newAmount);
        updatedTransaction.setCategory(newCategory);
        updatedTransaction.setDescription(newDescription);
        updatedTransaction.setUser(testUser);

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByIdAndUser(transactionId, testUser))
                .thenReturn(Optional.of(testTransaction));
        when(categoryService.findAccessibleCategoryByName(newCategoryName, userId))
                .thenReturn(Optional.of(newCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(updatedTransaction);

        // When
        Transaction result = transactionService.updateTransaction(
                transactionId, newAmount, newCategoryName, newDescription, userId);

        // Then
        assertEquals(updatedTransaction, result);
        verify(userService).findById(userId);
        verify(transactionRepository).findByIdAndUser(transactionId, testUser);
        verify(categoryService).findAccessibleCategoryByName(newCategoryName, userId);
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    @DisplayName("Should update transaction with partial fields")
    void shouldUpdateTransactionWithPartialFields() {
        // Given
        Long transactionId = 1L;
        Long userId = 1L;
        String newDescription = "Updated description";

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByIdAndUser(transactionId, testUser))
                .thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        Transaction result = transactionService.updateTransaction(
                transactionId, null, null, newDescription, userId);

        // Then
        assertEquals(testTransaction, result);
        verify(userService).findById(userId);
        verify(transactionRepository).findByIdAndUser(transactionId, testUser);
        verify(categoryService, never()).findAccessibleCategoryByName(any(), any());
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    @DisplayName("Should throw exception when updating with negative amount")
    void shouldThrowExceptionWhenUpdatingWithNegativeAmount() {
        // Given
        Long transactionId = 1L;
        Long userId = 1L;
        BigDecimal negativeAmount = new BigDecimal("-100.00");

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByIdAndUser(transactionId, testUser))
                .thenReturn(Optional.of(testTransaction));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.updateTransaction(transactionId, negativeAmount, null, null, userId)
        );

        assertEquals("Amount must be a positive decimal value.", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete transaction successfully")
    void shouldDeleteTransactionSuccessfully() {
        // Given
        Long transactionId = 1L;
        Long userId = 1L;

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByIdAndUser(transactionId, testUser))
                .thenReturn(Optional.of(testTransaction));

        // When
        transactionService.deleteTransaction(transactionId, userId);

        // Then
        verify(userService).findById(userId);
        verify(transactionRepository).findByIdAndUser(transactionId, testUser);
        verify(transactionRepository).delete(testTransaction);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent transaction")
    void shouldThrowExceptionWhenDeletingNonExistentTransaction() {
        // Given
        Long transactionId = 999L;
        Long userId = 1L;

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByIdAndUser(transactionId, testUser))
                .thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.deleteTransaction(transactionId, userId)
        );

        assertEquals("Transaction not found with ID: " + transactionId + " for user: " + userId,
                exception.getMessage());
        verify(transactionRepository, never()).delete(any());
    }
}