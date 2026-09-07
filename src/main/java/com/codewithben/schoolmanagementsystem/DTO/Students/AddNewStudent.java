package com.codewithben.schoolmanagementsystem.DTO.Students;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class AddNewStudent {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Date of birth is required")
    private String dateOfBirth;

    private String hometown;

    @NotBlank(message = "Parent name is required")
    private String parentName;

    @NotBlank(message = "Guardian contact is required")
    @Pattern(regexp = "\\d{10}", message = "Guardian contact must be 10 digits")
    private String guardianContact;

    @NotBlank(message = "Class (level) is required")
    private String levelId;

    @JsonProperty("isNew")
    private boolean isNew;

    @JsonProperty("isBorder")
    private boolean isBorder;
}
