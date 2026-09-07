package com.codewithben.schoolmanagementsystem.DTO.SpecialPayments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewSpecialPayment {

    @NotBlank(message = "Payment type is required")
    private String paymentType;

    //@NotBlank(message = "Amount is required")
    @Positive(message = "Amount is invalid")
    private float amount;

    @NotEmpty(message = "At least one class must be selected")
    private List<String> levelList;

    @NotBlank(message = "Description is required")
    private String description;
}
