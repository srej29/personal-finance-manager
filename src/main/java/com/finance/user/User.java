package com.finance.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "users") // Renamed from 'user' to 'users' to avoid potential SQL keyword conflicts
@Data // Lombok annotation for getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok annotation for no-arg constructor
@AllArgsConstructor // Lombok annotation for all-arg constructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // Valid email address

    @Column(nullable = false)
    private String password; // Hashed password

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String phoneNumber;

    // In a real application, you might add roles (e.g., @Enumerated(EnumType.STRING) private Role role;)
    // For this assignment, a simple user entity is sufficient.
}
