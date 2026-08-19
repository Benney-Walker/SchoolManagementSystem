package com.codewithben.schoolmanagementsystem.Messaging.Sms;

import com.codewithben.schoolmanagementsystem.DTO.Messaging.SmsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
public class HubtelSmsProvider implements SmsProvider {

    private final Logger logger = LoggerFactory.getLogger(HubtelSmsProvider.class);

    private final RestTemplate restTemplate;

    @Value("${hubtel.send-url}")
    private String SEND_URL;

    @Value("${hubtel.client-id}")
    private String clientId;

    @Value("${hubtel.client-secret}")
    private String clientSecret;

    @Value("${hubtel.sender-id}")
    private String senderId;

    public HubtelSmsProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public SmsResult send(String recipient, String message) {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {

            logger.warn("Hubtel credentials not configured; skipping SMS to {}", mask(recipient));
            return SmsResult.failed("Sms provider not configured");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(
                    Base64.getEncoder().encodeToString(
                            (clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8)));

            Map<String, String> body = Map.of(
                    "From", senderId,
                    "To", recipient,
                    "Content", message
            );

            ResponseEntity<String> response =
                    restTemplate.postForEntity(SEND_URL, new HttpEntity<>(body, headers), String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return SmsResult.success("Accepted by Hubtel");
            }

            logger.warn("Hubtel returned {} for SMS to {}", response.getStatusCode(), mask(recipient));
            return SmsResult.failed("Gateway status " + response.getStatusCode());
        } catch (Exception e) {
            logger.error("Hubtel send failed for {}", mask(recipient), e);
            return SmsResult.failed(e.getMessage());
        }
    }

    @Override
    public String name() {
        return "Hubtel";
    }

    private String mask(String number) {
        if (number == null || number.length() < 6) {
            return "***";
        }
        return number.substring(0, 4) + "****" + number.substring(number.length() - 2);
    }
}
