package com.codewithben.schoolmanagementsystem.DTO.Staff;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindStaffDTO {

    @NotBlank(message = "Staff ID is required")
    private String staffId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Surname is required")
    private String surname;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Date of birth is required")
    private String dateOfBirth;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @NotEmpty(message = "At least one role is required")
    private List<String> staffRoles;

    @NotBlank(message = "Staff status is required")
    private String staffStatus;

    private String dateOfRegistration;
}
