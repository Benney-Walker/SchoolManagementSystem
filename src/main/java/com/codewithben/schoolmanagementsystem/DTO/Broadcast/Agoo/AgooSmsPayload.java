package com.codewithben.schoolmanagementsystem.DTO.Broadcast.Agoo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgooSmsPayload {

    private String senderId;

    private String message;

    private String to;
}
