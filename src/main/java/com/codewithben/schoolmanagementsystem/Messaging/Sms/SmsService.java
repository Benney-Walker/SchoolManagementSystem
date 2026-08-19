package com.codewithben.schoolmanagementsystem.Messaging.Sms;

import com.codewithben.schoolmanagementsystem.DTO.Messaging.SmsResult;
import com.codewithben.schoolmanagementsystem.Utility.PhoneNumberUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmsService implements SmsInterface{

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    private final SmsProvider provider;

    private final PhoneNumberUtil phoneNumberUtil;

    public SmsService(SmsProvider provider,  PhoneNumberUtil phoneNumberUtil) {
        this.provider = provider;
        this.phoneNumberUtil = phoneNumberUtil;
    }

    @Override
    @Async
    public void sendSms(String recipient, String message) {
        String normalised = phoneNumberUtil.toInternational(recipient);
        if (normalised == null || message == null || message.isBlank()) {
            logger.warn("Skipping SMS: missing recipient or message");
            return;
        }

        SmsResult result = provider.send(normalised, message);
        if (result.isSuccess()) {
            logger.info("SMS sent via {} ({})", provider.name(), result.getDetail());
        } else {
            logger.warn("SMS not sent via {}: {}", provider.name(), result.getDetail());
        }
    }

    @Override
    @Async
    public void sendBulkSms(List<String> recipients, String message) {
        if (recipients == null || recipients.isEmpty() || message == null || message.isBlank()) {
            logger.warn("Skipping bulk SMS: no recipients or empty message");
            return;
        }

        int sent = 0;
        int failed = 0;
        for (String recipient : recipients) {
            String normalised = phoneNumberUtil.toInternational(recipient);
            if (normalised == null) {
                failed++;
                continue;
            }
            // Each send is independent; one failure never stops the rest.
            SmsResult result = provider.send(normalised, message);
            if (result.isSuccess()) {
                sent++;
            } else {
                failed++;
            }
        }
        logger.info("Bulk SMS via {} complete: {} sent, {} failed", provider.name(), sent, failed);
    }
}
