package com.finance.goal;

import com.finance.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String goalName;

    @Column(nullable = false, precision = 15, scale = 2) // Precision for monetary values
    private BigDecimal targetAmount;

    @Column(nullable = false)
    private LocalDate targetDate;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false, precision = 15, scale = 2) // Current progress towards the goal
    private BigDecimal currentProgress = BigDecimal.ZERO; // Initialize to zero

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Foreign key to users table
    private User user;
}
