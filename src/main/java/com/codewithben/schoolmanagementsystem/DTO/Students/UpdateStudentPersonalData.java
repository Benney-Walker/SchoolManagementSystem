package com.codewithben.schoolmanagementsystem.DTO.Students;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentPersonalData {
    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Date of birth is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date of birth must be in yyyy-MM-dd format")
    private String dateOfBirth;

    @NotBlank(message = "Parent name is required")
    private String parentName;

    @NotBlank(message = "Parent phone number is required")
    @Pattern(regexp = "\\d{10}", message = "Parent phone number must be 10 digits")
    private String parentPhoneNumber;

    @NotBlank(message = "Class (grade) is required")
    private String gradeId;

    private String status;

    private String homeTown;

}