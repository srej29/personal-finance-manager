package com.finance.transaction;

import com.finance.category.CategoryType;
import com.finance.transaction.dto.TransactionRequest;
import com.finance.transaction.dto.TransactionResponse;
import com.finance.exception.ResourceNotFoundException;
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
import java.util.Map;
import java.util.HashMap;




@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;

    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User not authenticated.");
        }
        String username = authentication.getName();
        return userService.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database: " + username))
                .getId();
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        try {
            Long userId = getAuthenticatedUserId();

            // Debug log to see what we're receiving
            System.out.println("Received transaction request: " + request);
            System.out.println("Category name: " + request.getCategoryName());

            Transaction newTransaction = transactionService.createTransaction(
                    request.getAmount(),
                    request.getDate(),
                    request.getCategoryName(),  // Make sure this is not null
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
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("Error creating transaction: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) CategoryType categoryType) {

        Long userId = getAuthenticatedUserId();
        List<Transaction> transactions = transactionService.getFilteredTransactions(
                userId, startDate, endDate, category, categoryType
        );

        List<TransactionResponse> responses = transactions.stream()
                .map(transaction -> new TransactionResponse(
                        transaction.getId(),
                        transaction.getAmount(),
                        transaction.getDate(),
                        transaction.getCategory().getName(),  // This becomes "category" in JSON
                        transaction.getCategory().getType(),
                        transaction.getDescription()
                ))
                .collect(Collectors.toList());

        // Return empty list directly, not wrapped in object
        return ResponseEntity.ok(responses);
    }

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

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        Long userId = getAuthenticatedUserId();

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTransaction(@PathVariable Long id) {
        Long userId = getAuthenticatedUserId();
        transactionService.deleteTransaction(id, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Transaction deleted successfully");
        return ResponseEntity.ok(response);
    }
}