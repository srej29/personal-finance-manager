package com.finance.transaction;

import com.finance.category.CategoryType;
import com.finance.transaction.dto.TransactionRequest;
import com.finance.transaction.dto.TransactionResponse;
import com.finance.exception.ResourceNotFoundException;
import com.finance.user.User;
import com.finance.user.UserService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService; // To get authenticated user ID

    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    /**
     * Helper method to get the authenticated user's ID.
     * This relies on Spring Security and the UserDetailsService implementation.
     * @return The ID of the authenticated user.
     * @throws IllegalStateException if no user is authenticated or user not found in DB.
     */
    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User not authenticated."); // This should be caught by Spring Security's filter chain (401)
        }
        String username = authentication.getName(); // Get username from authenticated principal
        return userService.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + username))
                .getId();
    }

    /**
     * Creates a new financial transaction for the authenticated user.
     * @param request The TransactionRequest DTO containing transaction details.
     * @return The created TransactionResponse DTO.
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        Long userId = getAuthenticatedUserId();
        Transaction newTransaction = transactionService.createTransaction(
                request.getAmount(),
                request.getDate(),
                request.getCategoryName(),
                request.getDescription(),
                userId
        );
        TransactionResponse response = new TransactionResponse(
                newTransaction.getId(),
                newTransaction.getAmount(),
                newTransaction.getDate(),
                newTransaction.getCategory().getName(),
                newTransaction.getCategory().getType(),
                newTransaction.getDescription()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves all transactions for the authenticated user, with optional filtering.
     * @param startDate Optional start date for filtering (format: YYYY-MM-DD).
     * @param endDate Optional end date for filtering (format: YYYY-MM-DD).
     * @param categoryName Optional category name for filtering.
     * @param categoryType Optional category type (INCOME/EXPENSE) for filtering.
     * @return A list of TransactionResponse DTOs.
     */
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) CategoryType categoryType) { // CategoryType will be automatically converted by Spring
        Long userId = getAuthenticatedUserId();
        List<Transaction> transactions = transactionService.getFilteredTransactions(
                userId, startDate, endDate, categoryName, categoryType
        );
        List<TransactionResponse> responses = transactions.stream()
                .map(transaction -> new TransactionResponse(
                        transaction.getId(),
                        transaction.getAmount(),
                        transaction.getDate(),
                        transaction.getCategory().getName(),
                        transaction.getCategory().getType(),
                        transaction.getDescription()
                ))
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * Retrieves a specific transaction by ID for the authenticated user.
     * @param id The ID of the transaction.
     * @return The TransactionResponse DTO.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        Transaction transaction = transactionService.getTransactionById(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));
        TransactionResponse response = new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getCategory().getName(),
                transaction.getCategory().getType(),
                transaction.getDescription()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Updates an existing transaction for the authenticated user.
     * @param id The ID of the transaction to update.
     * @param request The TransactionRequest DTO containing updated details (amount, categoryName, description).
     * @return The updated TransactionResponse DTO.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        Long userId = getAuthenticatedUserId();

        // Pass null for date as it cannot be updated
        Transaction updatedTransaction = transactionService.updateTransaction(
                id,
                request.getAmount(),
                request.getCategoryName(),
                request.getDescription(),
                userId
        );
        TransactionResponse response = new TransactionResponse(
                updatedTransaction.getId(),
                updatedTransaction.getAmount(),
                updatedTransaction.getDate(),
                updatedTransaction.getCategory().getName(),
                updatedTransaction.getCategory().getType(),
                updatedTransaction.getDescription()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Deletes a transaction for the authenticated user.
     * @param id The ID of the transaction to delete.
     * @return A success message.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransaction(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        transactionService.deleteTransaction(id, userId);
        return new ResponseEntity<>("Transaction deleted successfully", HttpStatus.OK);
    }
}
