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

    @Column(length = 100, nullable = false, unique = true)
    private String institutionName;

    @Column(nullable = false)
    private String broadcastHeader;

    // Printed under the school name on reports. Optional.
    @Column(length = 200)
    private String motto;

    // Printed under the motto on reports (e.g. town, region). Optional.
    @Column(length = 255)
    private String address;

    // Object URLs in the private storage bucket (see ObjectStorageService). The files are
    // downloaded and cached by BrandingAssetService; a new upload gets a new URL.
    @Column(length = 512)
    private String reportLogoUrl;

    @Column(length = 512)
    private String reportSignatureUrl;

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
    private List<Expenses> expenses;

    @OneToMany(mappedBy = "institution")
    private List<SpecialPayment> specialPayment;

    @OneToMany(mappedBy = "institution")
    private List<SchoolHoliday> schoolHolidays;
}
