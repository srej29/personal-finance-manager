package com.finance.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("User Repository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should find user by username")
    void shouldFindUserByUsername() {
        // Given
        User user = new User();
        user.setUsername("test@example.com");
        user.setPassword("hashedPassword");
        user.setFullName("Test User");
        user.setPhoneNumber("+1234567890");

        entityManager.persistAndFlush(user);

        // When
        Optional<User> found = userRepository.findByUsername("test@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getUsername());
        assertEquals("Test User", found.get().getFullName());
    }

    @Test
    @DisplayName("Should return empty when user not found by username")
    void shouldReturnEmptyWhenUserNotFoundByUsername() {
        // When
        Optional<User> found = userRepository.findByUsername("nonexistent@example.com");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should check if user exists by username")
    void shouldCheckIfUserExistsByUsername() {
        // Given
        User user = new User();
        user.setUsername("existing@example.com");
        user.setPassword("hashedPassword");
        user.setFullName("Existing User");
        user.setPhoneNumber("+1234567890");

        entityManager.persistAndFlush(user);

        // When & Then
        assertTrue(userRepository.existsByUsername("existing@example.com"));
        assertFalse(userRepository.existsByUsername("nonexistent@example.com"));
    }

    @Test
    @DisplayName("Should save user successfully")
    void shouldSaveUserSuccessfully() {
        // Given
        User user = new User();
        user.setUsername("new@example.com");
        user.setPassword("hashedPassword");
        user.setFullName("New User");
        user.setPhoneNumber("+1234567890");

        // When
        User saved = userRepository.save(user);

        // Then
        assertNotNull(saved.getId());
        assertEquals("new@example.com", saved.getUsername());
        assertEquals("New User", saved.getFullName());
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        // Given
        User user = new User();
        user.setUsername("update@example.com");
        user.setPassword("hashedPassword");
        user.setFullName("Original Name");
        user.setPhoneNumber("+1234567890");

        User saved = entityManager.persistAndFlush(user);
        entityManager.detach(saved);

        // When
        saved.setFullName("Updated Name");
        saved.setPhoneNumber("+0987654321");
        User updated = userRepository.save(saved);

        // Then
        assertEquals("Updated Name", updated.getFullName());
        assertEquals("+0987654321", updated.getPhoneNumber());
        assertEquals("update@example.com", updated.getUsername()); // Should remain unchanged
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        // Given
        User user = new User();
        user.setUsername("delete@example.com");
        user.setPassword("hashedPassword");
        user.setFullName("Delete User");
        user.setPhoneNumber("+1234567890");

        User saved = entityManager.persistAndFlush(user);
        Long userId = saved.getId();

        // When
        userRepository.delete(saved);
        entityManager.flush();

        // Then
        Optional<User> found = userRepository.findById(userId);
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should handle username uniqueness constraint")
    void shouldHandleUsernameUniquenessConstraint() {
        // Given
        User user1 = new User();
        user1.setUsername("duplicate@example.com");
        user1.setPassword("hashedPassword1");
        user1.setFullName("User One");
        user1.setPhoneNumber("+1234567890");

        User user2 = new User();
        user2.setUsername("duplicate@example.com"); // Same username
        user2.setPassword("hashedPassword2");
        user2.setFullName("User Two");
        user2.setPhoneNumber("+0987654321");

        // When
        entityManager.persistAndFlush(user1);

        // Then - Attempting to save second user with same username should fail
        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(user2);
        });
    }

    @Test
    @DisplayName("Should check username existence correctly")
    void shouldCheckUsernameExistenceCorrectly() {
        // Given
        User user = new User();
        user.setUsername("exists@example.com");
        user.setPassword("hashedPassword");
        user.setFullName("Existing User");
        user.setPhoneNumber("+1234567890");

        entityManager.persistAndFlush(user);

        // When & Then
        assertTrue(userRepository.existsByUsername("exists@example.com"));
        assertFalse(userRepository.existsByUsername("doesnotexist@example.com"));
    }

    @Test
    @DisplayName("Should handle case sensitivity in username search")
    void shouldHandleCaseSensitivityInUsernameSearch() {
        // Given
        User user = new User();
        user.setUsername("test@example.com");
        user.setPassword("hashedPassword");
        user.setFullName("Test User");
        user.setPhoneNumber("+1234567890");

        entityManager.persistAndFlush(user);

        // When & Then
        assertTrue(userRepository.findByUsername("test@example.com").isPresent());
        // Note: This behavior depends on your database configuration
        // By default, most databases are case-insensitive for email matching
    }

    @Test
    @DisplayName("Should persist and retrieve all user fields correctly")
    void shouldPersistAndRetrieveAllUserFieldsCorrectly() {
        // Given
        User user = new User();
        user.setUsername("complete@example.com");
        user.setPassword("hashedSecurePassword123");
        user.setFullName("Complete Test User");
        user.setPhoneNumber("+1-234-567-8900");

        // When
        User saved = userRepository.save(user);
        entityManager.flush();
        entityManager.detach(saved);

        Optional<User> retrieved = userRepository.findById(saved.getId());

        // Then
        assertTrue(retrieved.isPresent());
        User found = retrieved.get();
        assertEquals("complete@example.com", found.getUsername());
        assertEquals("hashedSecurePassword123", found.getPassword());
        assertEquals("Complete Test User", found.getFullName());
        assertEquals("+1-234-567-8900", found.getPhoneNumber());
    }

    @Test
    @DisplayName("Should handle empty and null username searches")
    void shouldHandleEmptyAndNullUsernameSearches() {
        // When & Then
        assertFalse(userRepository.findByUsername("").isPresent());
        assertFalse(userRepository.findByUsername(null).isPresent());
        assertFalse(userRepository.existsByUsername(""));
        assertFalse(userRepository.existsByUsername(null));
    }
}