package com.finance.goal;

import com.finance.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    /**
     * Finds all goals for a specific user.
     * @param user The user.
     * @return A list of goals.
     */
    List<Goal> findByUser(User user);

    /**
     * Finds a goal by ID and ensures it belongs to the specified user.
     * @param id The goal ID.
     * @param user The user.
     * @return An Optional containing the goal if found and owned by the user, or empty.
     */
    Optional<Goal> findByIdAndUser(Long id, User user);
}
