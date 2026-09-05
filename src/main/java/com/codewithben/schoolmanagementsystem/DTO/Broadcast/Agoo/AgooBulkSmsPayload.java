package com.codewithben.schoolmanagementsystem.DTO.Broadcast.Agoo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgooBulkSmsPayload {

    private String senderId;

    private String message;

    private List<String> recipients;
}
