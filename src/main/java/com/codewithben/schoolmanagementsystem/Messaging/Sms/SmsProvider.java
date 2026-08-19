package com.codewithben.schoolmanagementsystem.Messaging.Sms;

import com.codewithben.schoolmanagementsystem.DTO.Messaging.SmsResult;

public interface SmsProvider {

    SmsResult send(String recipient, String message);

    String name();
}
