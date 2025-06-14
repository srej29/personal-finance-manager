package com.finance.auth.dto; // Corrected package name

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok annotation for getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok annotation for no-arg constructor
@AllArgsConstructor // Lombok annotation for all-arg constructor
public class AuthResponse {
    private String message;
    private Long userId; // Optional, useful for registration success
}
