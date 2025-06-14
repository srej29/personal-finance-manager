package com.finance.auth.dto; // Placed in a 'dto' subpackage for better organization

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // Lombok annotation for getters, setters, toString, equals, hashCode
public class LoginRequest {

    @NotBlank(message = "Username cannot be empty")
    @Email(message = "Username must be a valid email address") // Ensure it's a valid email format for consistency
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}
