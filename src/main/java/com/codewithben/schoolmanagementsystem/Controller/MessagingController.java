package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Broadcast.EmailRequest;
import com.codewithben.schoolmanagementsystem.DTO.Broadcast.SmsRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/message")
public class MessagingController {

    @PostMapping("/v1/broadcast-sms")
    public ResponseEntity<?> smsRequest(@RequestBody SmsRequest smsRequest) {
         return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/broadcast-email")
    public ResponseEntity<?> emailRequest(@RequestBody EmailRequest emailRequest) {
        return ResponseEntity.ok().build();
    }
}
