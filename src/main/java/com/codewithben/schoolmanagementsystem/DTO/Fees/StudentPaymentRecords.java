package com.codewithben.schoolmanagementsystem.DTO.Fees;

import jakarta.validation.constraints.NotBlank;
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
public class StudentPaymentRecords {

    @NotBlank(message = "Payment ID is required")
    private String paymentId;

    private String dateOfPayment;

    @NotBlank(message = "Student ID is required")
    private String studentId;

    private String studentName;

    @Positive(message = "Amount must be positive value")
    private float amount;

    @NotBlank(message = "Semester is required")
    private String semesterId;

    @NotBlank(message = "Payer name is required")
    private String payerName;

    @Pattern(regexp = "\\d{10}", message = "Payer contact must be 10 digits")
    private String payerContact;
}
