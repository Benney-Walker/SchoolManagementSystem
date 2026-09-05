package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Broadcast.SmsRequest;
import com.codewithben.schoolmanagementsystem.Service.BroadcastService;
import com.codewithben.schoolmanagementsystem.Utility.AuthenticatedStaffProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/message")
public class MessagingController {

    private final BroadcastService broadcastService;

    private final AuthenticatedStaffProvider authenticatedStaffProvider;

    @PostMapping("/v1/broadcast-sms")
    public ResponseEntity<?> smsRequest(@Valid @RequestBody SmsRequest smsRequest) {

        String staffId = authenticatedStaffProvider.getStaffId();

         return broadcastService.broadcastCustomBulkSms(smsRequest, staffId);
    }

    @PostMapping("/v1/broadcast-email")
    public ResponseEntity<?> emailRequest() {
        return ResponseEntity.notFound().build();
    }
}
