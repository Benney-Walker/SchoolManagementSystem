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
public class Institution {

    @Id
    private String institutionId;

    @Column(length = 100, nullable = false)
    private String institutionName;

    @OneToMany(mappedBy = "institution")
    private List<Staffs> staff;

    @OneToMany(mappedBy = "institution")
    private List<Students> students;

    @OneToMany(mappedBy = "institution")
    private List<Level> level;

    @OneToMany(mappedBy = "institution")
    private List<Semester> semester;

    @OneToMany(mappedBy = "institution")
    private List<Fees> fees;

    @OneToMany(mappedBy = "institution")
    private List<PaymentRecords> paymentRecords;
}
