package com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Fees;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentReport {

    private String studentId;

    private float amountPaid;

    private float newBalance;
}
