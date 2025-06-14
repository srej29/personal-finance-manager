package com.finance.user;

import com.finance.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test@example.com");
        testUser.setPassword("rawPassword123");
        testUser.setFullName("John Doe");
        testUser.setPhoneNumber("+1234567890");
    }

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUserSuccessfully() {
        // Given
        String rawPassword = "securePassword123";
        String encodedPassword = "encodedPassword123";

        User userToRegister = new User();
        userToRegister.setUsername("newuser@example.com");
        userToRegister.setPassword(rawPassword);
        userToRegister.setFullName("New User");
        userToRegister.setPhoneNumber("+1234567890");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("newuser@example.com");
        savedUser.setPassword(encodedPassword);
        savedUser.setFullName("New User");
        savedUser.setPhoneNumber("+1234567890");

        when(userRepository.existsByUsername("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = userService.registerUser(userToRegister);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("newuser@example.com", result.getUsername());
        assertEquals(encodedPassword, result.getPassword());
        assertEquals("New User", result.getFullName());
        assertEquals("+1234567890", result.getPhoneNumber());

        verify(userRepository).existsByUsername("newuser@example.com");
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(userToRegister);

        // Verify that the password was set to encoded value before saving
        assertEquals(encodedPassword, userToRegister.getPassword());
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Given
        User userToRegister = new User();
        userToRegister.setUsername("existing@example.com");
        userToRegister.setPassword("password123");
        userToRegister.setFullName("Existing User");
        userToRegister.setPhoneNumber("+1234567890");

        when(userRepository.existsByUsername("existing@example.com")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(userToRegister)
        );

        assertEquals("Username already taken.", exception.getMessage());
        verify(userRepository).existsByUsername("existing@example.com");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should authenticate user successfully")
    void shouldAuthenticateUserSuccessfully() {
        // Given
        String username = "test@example.com";
        String rawPassword = "correctPassword";
        String encodedPassword = "encodedCorrectPassword";

        User storedUser = new User();
        storedUser.setId(1L);
        storedUser.setUsername(username);
        storedUser.setPassword(encodedPassword);
        storedUser.setFullName("Test User");
        storedUser.setPhoneNumber("+1234567890");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(storedUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // When
        User result = userService.authenticateUser(username, rawPassword);

        // Then
        assertNotNull(result);
        assertEquals(storedUser, result);
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    @DisplayName("Should throw exception when user not found during authentication")
    void shouldThrowExceptionWhenUserNotFoundDuringAuthentication() {
        // Given
        String username = "nonexistent@example.com";
        String rawPassword = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.authenticateUser(username, rawPassword)
        );

        assertEquals("User not found with username: " + username, exception.getMessage());
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when password doesn't match")
    void shouldThrowExceptionWhenPasswordDoesntMatch() {
        // Given
        String username = "test@example.com";
        String rawPassword = "wrongPassword";
        String encodedPassword = "encodedCorrectPassword";

        User storedUser = new User();
        storedUser.setId(1L);
        storedUser.setUsername(username);
        storedUser.setPassword(encodedPassword);
        storedUser.setFullName("Test User");
        storedUser.setPhoneNumber("+1234567890");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(storedUser));
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.authenticateUser(username, rawPassword)
        );

        assertEquals("Invalid credentials.", exception.getMessage());
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    @DisplayName("Should find user by ID successfully")
    void shouldFindUserByIdSuccessfully() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should return empty when user not found by ID")
    void shouldReturnEmptyWhenUserNotFoundById() {
        // Given
        Long userId = 999L;

        // When (don't stub findById - let it return empty naturally)
        Optional<User> result = userService.findById(userId);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should find user by username successfully")
    void shouldFindUserByUsernameSuccessfully() {
        // Given
        String username = "test@example.com";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByUsername(username);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("Should return empty when user not found by username")
    void shouldReturnEmptyWhenUserNotFoundByUsername() {
        // Given
        String username = "nonexistent@example.com";

        // When (don't stub findByUsername - let it return empty naturally)
        Optional<User> result = userService.findByUsername(username);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("Should handle empty password during authentication")
    void shouldHandleEmptyPasswordDuringAuthentication() {
        // Given
        String username = "test@example.com";
        String emptyPassword = "";
        String encodedPassword = "encodedCorrectPassword";

        User storedUser = new User();
        storedUser.setUsername(username);
        storedUser.setPassword(encodedPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(storedUser));
        when(passwordEncoder.matches(emptyPassword, encodedPassword)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.authenticateUser(username, emptyPassword)
        );

        assertEquals("Invalid credentials.", exception.getMessage());
    }

    @Test
    @DisplayName("Should register user and modify original user object password")
    void shouldRegisterUserAndModifyOriginalUserObjectPassword() {
        // Given
        String rawPassword = "originalPassword";
        String encodedPassword = "encodedPassword";

        User userToRegister = new User();
        userToRegister.setUsername("test@example.com");
        userToRegister.setPassword(rawPassword);
        userToRegister.setFullName("Test User");
        userToRegister.setPhoneNumber("+1234567890");

        when(userRepository.existsByUsername("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(userToRegister)).thenReturn(userToRegister);

        // When
        userService.registerUser(userToRegister);

        // Then
        assertEquals(encodedPassword, userToRegister.getPassword()); // Original object should be modified
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(userToRegister);
    }
}