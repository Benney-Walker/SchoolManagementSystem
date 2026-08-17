package com.codewithben.schoolmanagementsystem.DTO.SpecialPayments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialPaymentRecord {

    private String id;

    private String type;

    private int amount;

    private String levelName;

    private String description;

    private String createdAt;
}
