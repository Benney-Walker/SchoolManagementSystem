package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.DTO.Broadcast.Agoo.AgooBulkSmsPayload;
import com.codewithben.schoolmanagementsystem.DTO.Broadcast.Agoo.AgooSmsPayload;
import com.codewithben.schoolmanagementsystem.DTO.Broadcast.Agoo.AgooSmsResponse;
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

    @Value("${agoo.sms.sender.id}")
    private String SENDER_ID;

    @Value("${agoo.sms.url}")
    private String AGOO_API_URL;

    @Value("${agoo.bulk.sms.url}")
    private String AGOO_BULK_API_URL;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public AgooSmsResponse sendBulkSms(String message, List<String> recipients) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", API_KEY);

        AgooBulkSmsPayload payload = AgooBulkSmsPayload.builder()
                .message(message)
                .recipients(recipients)
                .senderId(SENDER_ID)
                .build();

        HttpEntity<AgooBulkSmsPayload> request = new HttpEntity<>(payload, headers);

        try {

            ResponseEntity<AgooSmsResponse> response = restTemplate.postForEntity(
                    AGOO_BULK_API_URL, request, AgooSmsResponse.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Could not send bulk sms messages: " + e.getMessage());
            return null;
        }
    }

    @Override
    public AgooSmsResponse sendSms(String message, String recipient) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", API_KEY);

        AgooSmsPayload agooSmsPayload = AgooSmsPayload.builder()
                .senderId(SENDER_ID)
                .message(message)
                .to(recipient)
                .build();

        HttpEntity<AgooSmsPayload> request = new HttpEntity<>(agooSmsPayload, headers);

        try {

            ResponseEntity<AgooSmsResponse> response = restTemplate.postForEntity(
                    AGOO_API_URL, request, AgooSmsResponse.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Could not send bulk sms messages: " + e.getMessage());
            return null;
        }
    }
}
