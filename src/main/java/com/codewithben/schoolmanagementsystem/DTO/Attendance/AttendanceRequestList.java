package com.codewithben.schoolmanagementsystem.DTO.Attendance;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequestList {

    @NotEmpty(message = "Student Id can't be empty")
    private String studentId;

    @NotEmpty(message = "Attendance status can't be empty")
    private String status;
}
