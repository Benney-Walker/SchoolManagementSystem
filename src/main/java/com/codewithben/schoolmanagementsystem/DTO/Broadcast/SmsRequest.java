package com.codewithben.schoolmanagementsystem.DTO.Broadcast;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {

    private String message;

    private List<String> audience;
}
