package com.codewithben.schoolmanagementsystem.DTO.Broadcast.Agoo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgooSmsMeta {

    private String requestId;

    private Instant timestamp;
}
