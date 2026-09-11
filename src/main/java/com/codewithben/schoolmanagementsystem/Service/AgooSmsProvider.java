/*package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Broadcast.Agoo.AgooBulkSmsPayload;
import com.codewithben.schoolmanagementsystem.DTO.Broadcast.Agoo.AgooSmsPayload;
import com.codewithben.schoolmanagementsystem.DTO.Broadcast.Agoo.AgooSmsResponse;
import com.codewithben.schoolmanagementsystem.DTO.Broadcast.SmsResponse;
import com.codewithben.schoolmanagementsystem.Entity.Messages;
import com.codewithben.schoolmanagementsystem.Interface.SmsInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
public class AgooSmsProvider implements SmsInterface {

    @Value("${agoo.sms.api.key}")
    private String API_KEY;

    @Value("${agoo.sms.url}")
    private String AGOO_API_URL;

    @Value("${agoo.bulk.sms.url}")
    private String AGOO_BULK_API_URL;

    @Value("${agoo.sms.sender.id}")
    private String AGOO_SENDER_ID;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public SmsResponse sendBulkSms(String message, List<String> recipients) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", API_KEY);

        AgooBulkSmsPayload payload = AgooBulkSmsPayload.builder()
                .message(message)
                .recipients(recipients)
                .senderId(AGOO_SENDER_ID)
                .build();

        HttpEntity<AgooBulkSmsPayload> request = new HttpEntity<>(payload, headers);

        SmsResponse smsResponse;
        try {

            ResponseEntity<AgooSmsResponse> response = restTemplate.postForEntity(
                    AGOO_BULK_API_URL, request, AgooSmsResponse.class
            );

            AgooSmsResponse agooResponse = response.getBody();
            smsResponse = SmsResponse.builder()
                    .success(agooResponse.isSuccess())
                    .audienceCount(agooResponse.getData().getRecipientCount())
                    .smsCost(agooResponse.getData().getTotalCost())
                    .build();
            return smsResponse;
        } catch (Exception e) {
            log.error("Could not send bulk sms messages: " + e.getMessage());
            smsResponse = SmsResponse.builder()
                    .success(false)
                    .build();
            return smsResponse;
        }
    }

    @Override
    public SmsResponse sendSms(String message, String recipient) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", API_KEY);

        AgooSmsPayload agooSmsPayload = AgooSmsPayload.builder()
                .senderId(AGOO_SENDER_ID)
                .message(message)
                .to(recipient)
                .build();

        HttpEntity<AgooSmsPayload> request = new HttpEntity<>(agooSmsPayload, headers);

        SmsResponse smsResponse;
        try {

            ResponseEntity<AgooSmsResponse> response = restTemplate.postForEntity(
                    AGOO_API_URL, request, AgooSmsResponse.class
            );
            AgooSmsResponse agooResponse = response.getBody();

            smsResponse = SmsResponse.builder()
                    .smsCost(agooResponse.getData().getTotalCost())
                    .success(agooResponse.isSuccess())
                    .audienceCount(agooResponse.getData().getRecipientCount())
                    .build();

            return smsResponse;
        } catch (Exception e) {
            log.error("Could not send sms message: " + e.getMessage());
            smsResponse = SmsResponse.builder()
                    .success(false)
                    .build();
            return smsResponse;
        }
    }
}*/
