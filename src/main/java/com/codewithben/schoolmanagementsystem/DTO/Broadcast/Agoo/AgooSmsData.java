package com.codewithben.schoolmanagementsystem.DTO.Broadcast.Agoo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgooSmsData {

    private String messageId;

    private int recipientCount;

    private int segments;

    private float totalCost;

    private float balance;

    private String status;
}
