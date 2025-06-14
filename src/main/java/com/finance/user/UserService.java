package com.finance.user;

import com.finance.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user.
     * Encodes the password before saving.
     * @param user The user object containing registration details.
     * @return The registered User object.
     * @throws IllegalArgumentException if a user with the given username already exists.
     */
    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already taken.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Hash the password
        return userRepository.save(user);
    }

    /**
     * Authenticates a user.
     * @param username The user's username (email).
     * @param rawPassword The raw password provided by the user.
     * @return The authenticated User object.
     * @throws ResourceNotFoundException if the user is not found.
     * @throws IllegalArgumentException if the password does not match.
     */
    public User authenticateUser(String username, String rawPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found with username: " + username);
        }
        User user = userOptional.get();
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }
        return user;
    }

    /**
     * Finds a user by their ID.
     * @param id The ID of the user.
     * @return An Optional containing the User if found, or empty.
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Finds a user by their username.
     * @param username The username (email) of the user.
     * @return An Optional containing the User if found, or empty.
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
