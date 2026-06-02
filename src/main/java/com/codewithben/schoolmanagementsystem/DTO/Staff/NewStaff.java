package com.codewithben.schoolmanagementsystem.DTO.Staff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewStaff {

    private String firstName;

    private String lastName;

    private String gender;

    private String dateOfBirth;

    private String password;

    private String email;

    private String phoneNumber;

    private List<String> roles;
}
