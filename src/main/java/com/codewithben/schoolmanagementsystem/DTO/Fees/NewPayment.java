package com.codewithben.schoolmanagementsystem.DTO.Fees;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewPayment {
    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotNull(message = "Amount paid is required")
    @Positive(message = "Amount paid must be greater than zero")
    private Double amountPaid;

    @NotBlank(message = "Payer name is required")
    private String payerName;

    @NotBlank(message = "Payer phone is required")
    @Pattern(regexp = "\\d{10}", message = "Payer phone must be 10 digits")
    private String payerPhone;

    @NotBlank(message = "Class (level) is required")
    private String levelId;

    @NotBlank(message = "Semester is required")
    private String semesterId;
}
