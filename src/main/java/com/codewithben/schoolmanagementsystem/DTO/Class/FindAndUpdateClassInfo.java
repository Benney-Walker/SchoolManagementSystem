package com.codewithben.schoolmanagementsystem.DTO.Class;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindAndUpdateClassInfo {

    @NotBlank(message = "Class (level) ID is required")
    private String levelId;

    @NotBlank(message = "Class name is required")
    private String levelName;

    @NotBlank(message = "Instructor (staff) is required")
    private String staffId;
}
