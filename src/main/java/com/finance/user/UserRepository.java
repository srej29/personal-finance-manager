package com.finance.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by username (email).
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a user exists by username.
     */
    boolean existsByUsername(String username);
}