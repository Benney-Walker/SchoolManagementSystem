package com.codewithben.schoolmanagementsystem.DTO.Students;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindStudentDTO {
    
    private String studentId;

    private String firstName;

    private String lastName;

    private String gender;

    private String dateOfBirth;

    private String parentName;

    private String parentPhoneNumber;
    
    // Null for a graduated student (no current class).
    private String gradeId;

    private String status;

    private String homeTown;

    // Set only for graduated students; null otherwise.
    private String graduationDate;

    private String graduationClassName;

    private String graduationAcademicYear;

}
