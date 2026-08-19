package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class LevelSpecialPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private double amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private Level level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "special_payment_id", nullable = false)
    private SpecialPayment specialPayment;

    private String description;

    private LocalDate createdAt;

    private LocalDate updatedAt;
}
