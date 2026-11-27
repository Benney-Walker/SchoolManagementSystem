package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Expenses {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String expenseId;

    @Column(length = 100, nullable = false)
    private String description;

    @Column(nullable = false)
    private float amountSpent;

    @Column(length = 500)
    private String extraInfo;

    private LocalDate expenseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_semesterId",  nullable = false)
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    private Institution institution;

    /*
     * Soft delete: a deleted expense keeps its row, with when and by whom, so the term's
     * spending can still be audited. Null means not deleted (existing rows need no update).
     * Every read goes through ExpensesRepo's ...DeletedAtIsNull finders.
     */
    private LocalDateTime deletedAt;

    @Column(length = 64)
    private String deletedBy;
}
