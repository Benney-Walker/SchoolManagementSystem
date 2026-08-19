package com.codewithben.schoolmanagementsystem.DTO.Messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsResult {

    private boolean success;

    private String detail;

    public static SmsResult success(String detail) {
        return new SmsResult(true, detail);
    }

    public static SmsResult failed(String detail) {
        return new SmsResult(false, detail);
    }
}
