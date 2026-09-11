package com.codewithben.schoolmanagementsystem.Interface;

import com.codewithben.schoolmanagementsystem.DTO.Broadcast.SmsResponse;

import java.util.List;

public interface SmsInterface {

    SmsResponse sendBulkSms(String message, List<String> recipient);

    SmsResponse sendSms(String message, String recipient);
}
