package com.finance.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    @DisplayName("Should create user with all args constructor")
    void shouldCreateUserWithAllArgsConstructor() {
        // Given
        Long id = 1L;
        String username = "test@example.com";
        String password = "hashedPassword123";
        String fullName = "John Doe";
        String phoneNumber = "+1234567890";

        // When
        User user = new User(id, username, password, fullName, phoneNumber);

        // Then
        assertAll(
                () -> assertEquals(id, user.getId()),
                () -> assertEquals(username, user.getUsername()),
                () -> assertEquals(password, user.getPassword()),
                () -> assertEquals(fullName, user.getFullName()),
                () -> assertEquals(phoneNumber, user.getPhoneNumber())
        );
    }

    @Test
    @DisplayName("Should create user with no args constructor")
    void shouldCreateUserWithNoArgsConstructor() {
        // When
        User user = new User();

        // Then
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getFullName());
        assertNull(user.getPhoneNumber());
    }

    @Test
    @DisplayName("Should set and get username")
    void shouldSetAndGetUsername() {
        // Given
        String username = "test@example.com";

        // When
        user.setUsername(username);

        // Then
        assertEquals(username, user.getUsername());
    }

    @Test
    @DisplayName("Should set and get password")
    void shouldSetAndGetPassword() {
        // Given
        String password = "securePassword123";

        // When
        user.setPassword(password);

        // Then
        assertEquals(password, user.getPassword());
    }

    @Test
    @DisplayName("Should set and get full name")
    void shouldSetAndGetFullName() {
        // Given
        String fullName = "Jane Smith";

        // When
        user.setFullName(fullName);

        // Then
        assertEquals(fullName, user.getFullName());
    }

    @Test
    @DisplayName("Should set and get phone number")
    void shouldSetAndGetPhoneNumber() {
        // Given
        String phoneNumber = "+9876543210";

        // When
        user.setPhoneNumber(phoneNumber);

        // Then
        assertEquals(phoneNumber, user.getPhoneNumber());
    }

    @Test
    @DisplayName("Should set and get id")
    void shouldSetAndGetId() {
        // Given
        Long id = 100L;

        // When
        user.setId(id);

        // Then
        assertEquals(id, user.getId());
    }

    @Test
    @DisplayName("Should handle equals and hashCode correctly")
    void shouldHandleEqualsAndHashCodeCorrectly() {
        // Given
        User user1 = new User(1L, "test@example.com", "password", "John Doe", "+1234567890");
        User user2 = new User(1L, "test@example.com", "password", "John Doe", "+1234567890");
        User user3 = new User(2L, "other@example.com", "password", "Jane Doe", "+0987654321");

        // Then
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @Test
    @DisplayName("Should generate meaningful toString")
    void shouldGenerateMeaningfulToString() {
        // Given
        user.setId(1L);
        user.setUsername("test@example.com");
        user.setFullName("John Doe");
        user.setPhoneNumber("+1234567890");

        // When
        String toString = user.toString();

        // Then
        assertAll(
                () -> assertTrue(toString.contains("id=1")),
                () -> assertTrue(toString.contains("username=test@example.com")),
                () -> assertTrue(toString.contains("fullName=John Doe")),
                () -> assertTrue(toString.contains("phoneNumber=+1234567890"))
        );
    }

    @Test
    @DisplayName("Should not be equal to null")
    void shouldNotBeEqualToNull() {
        // Given
        User user = new User(1L, "test@example.com", "password", "John Doe", "+1234567890");

        // Then
        assertNotEquals(user, null);
    }

    @Test
    @DisplayName("Should not be equal to different class")
    void shouldNotBeEqualToDifferentClass() {
        // Given
        User user = new User(1L, "test@example.com", "password", "John Doe", "+1234567890");
        String notAUser = "I'm not a user";

        // Then
        assertNotEquals(user, notAUser);
    }
}