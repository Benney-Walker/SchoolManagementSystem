package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Expenses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long expenseId;

    @Column(length = 100, nullable = false)
    private String description;

    @Column(nullable = false)
    private int amountSpent;

    @Column(length = 500)
    private String extraInfo;

    private LocalDate expenseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_semesterId",  nullable = false)
    private Semester semester;
}
