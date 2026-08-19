package com.codewithben.schoolmanagementsystem.Messaging.Sms;

import java.util.List;

public interface SmsInterface {

    void sendSms(String recipient, String message);

    void sendBulkSms(List<String> recipients, String message);
}
