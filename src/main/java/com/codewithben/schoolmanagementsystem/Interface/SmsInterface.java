package com.codewithben.schoolmanagementsystem.Interface;

import com.codewithben.schoolmanagementsystem.DTO.Broadcast.Agoo.AgooSmsResponse;

import java.util.List;

public interface SmsInterface {

    AgooSmsResponse sendBulkSms(String senderId, String message, List<String> recipient);

    AgooSmsResponse sendSms(String senderId, String message, String recipient);
}
