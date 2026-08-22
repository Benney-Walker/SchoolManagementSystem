package com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Fees;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeesUpdate {

    private String feesId;
}
