package com.codewithben.schoolmanagementsystem.Interface;

import com.codewithben.schoolmanagementsystem.DTO.Broadcast.Agoo.AgooSmsResponse;

import java.util.List;

public interface SmsInterface {

    AgooSmsResponse sendBulkSms(String message, List<String> recipient);
}
