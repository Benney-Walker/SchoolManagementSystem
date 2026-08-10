package com.codewithben.schoolmanagementsystem.DTO.Subject;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddNewSubject {

    @NotBlank(message = "Subject name is required")
    private String subjectName;

    @NotBlank(message = "Class (grade) is required")
    private String gradeId;
}
