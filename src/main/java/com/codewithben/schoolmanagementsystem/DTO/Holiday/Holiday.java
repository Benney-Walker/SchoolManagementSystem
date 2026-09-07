package com.codewithben.schoolmanagementsystem.DTO.Holiday;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Holiday {

    private int holidayId;

    @NotBlank(message = "Holiday name is required")
    private String holidayName;

    @NotBlank(message = "Start date is required")
    private String startDate;

    @NotBlank(message = "End date is required")
    private String endDate;
}
