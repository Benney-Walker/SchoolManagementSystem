package com.codewithben.schoolmanagementsystem.Messaging;

import java.util.List;

public interface SmsInterface {

    void sendSms(String recipient, String message);

    void sendBulkSms(List<String> recipients, String message);
}
