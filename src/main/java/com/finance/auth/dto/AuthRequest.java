package com.finance.auth.dto; // Corrected package name

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data // Lombok annotation for getters, setters, toString, equals, hashCode
public class AuthRequest {

    @NotBlank(message = "Username cannot be empty")
    @Email(message = "Username must be a valid email address")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "Full name cannot be empty")
    private String fullName;

    @NotBlank(message = "Phone number cannot be empty")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number format")
    private String phoneNumber;
}
