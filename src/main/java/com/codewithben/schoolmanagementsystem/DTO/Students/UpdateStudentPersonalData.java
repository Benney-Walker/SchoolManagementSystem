package com.codewithben.schoolmanagementsystem.DTO.Students;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentPersonalData {
    private String studentId;

    private String firstName;

    private String lastName;

    private String gender;

    private String dateOfBirth;

    private String parentName;

    private String parentPhoneNumber;

    private String gradeId;

    private String status;

    private String homeTown;

}
