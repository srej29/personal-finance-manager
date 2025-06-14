package com.finance.exception;

import com.finance.auth.dto.AuthResponse; // Import AuthResponse DTO
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Changed return type to ResponseEntity<AuthResponse> for consistency
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<AuthResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(new AuthResponse(ex.getMessage(), null), HttpStatus.NOT_FOUND);
    }

    // Changed return type to ResponseEntity<AuthResponse> for consistency
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        if (ex.getMessage().contains("Username already taken") || ex.getMessage().contains("already exists")) {
            return new ResponseEntity<>(new AuthResponse(ex.getMessage(), null), HttpStatus.CONFLICT); // 409 Conflict
        }
        return new ResponseEntity<>(new AuthResponse(ex.getMessage(), null), HttpStatus.BAD_REQUEST); // 400 Bad Request
    }

    // Changed return type to ResponseEntity<AuthResponse> for consistency
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<AuthResponse> handleBadCredentialsException(BadCredentialsException ex) {
        return new ResponseEntity<>(new AuthResponse("Invalid credentials", null), HttpStatus.UNAUTHORIZED); // 401 Unauthorized
    }

    // Changed return type to ResponseEntity<AuthResponse> for consistency
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> handleGlobalException(Exception ex) {
        System.err.println("An unexpected error occurred: " + ex.getMessage());
        ex.printStackTrace();
        return new ResponseEntity<>(new AuthResponse("An unexpected error occurred. Please try again later.", null), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
