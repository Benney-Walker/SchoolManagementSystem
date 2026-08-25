package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Offline.OfflineAttendanceList;
import com.codewithben.schoolmanagementsystem.DTO.Offline.OfflinePaymentList;
import com.codewithben.schoolmanagementsystem.DTO.Offline.OfflineScoresList;
import com.codewithben.schoolmanagementsystem.Service.OfflineReplayService;
import com.codewithben.schoolmanagementsystem.Utility.AuthenticatedStaffProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/offline")
public class OfflineReplayController {

    private final OfflineReplayService offlineReplayService;

    private final AuthenticatedStaffProvider authenticatedStaffProvider;

    @PostMapping("/v1/sync-attendance")
    public ResponseEntity<?> saveAttendanceRecords(@RequestBody List<OfflineAttendanceList> list) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return offlineReplayService.saveOfflineAttendanceRecords(staffId, list);
    }

    @PostMapping("/v1/sync-scores")
    public ResponseEntity<?> saveScoresRecords(@RequestBody List<OfflineScoresList> list) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return offlineReplayService.saveOfflineScores(staffId, list);
    }

    @PostMapping("/v1/sync-payment")
    public ResponseEntity<?> savePaymentsRecords(@RequestBody List<OfflinePaymentList> list) {

        String staffId = authenticatedStaffProvider.getStaffId();

        return offlineReplayService.saveOfflinePayments(staffId, list);
    }
}
