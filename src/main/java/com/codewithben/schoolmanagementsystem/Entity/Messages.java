package com.codewithben.schoolmanagementsystem.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Messages {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String messageId;

    @Column(length = 1000, nullable = false)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    private Staffs sentBy;

    private List<String> audience;

    private int audienceCount;

    private float smsCost;

    private int successCount;

    private int failureCount;
}
