package com.codewithben.schoolmanagementsystem.DTO.Broadcast;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull(message = "Message cannot be empty")
    @Size(max = 350, message = "Message cannot exceed 480 characters")
    private String message;

    @NotEmpty(message = "At least an audience must be selected")
    @NotNull(message = "At least an audience must be selected")
    private List<String> audience;
}
