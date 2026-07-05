package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.Offline.OfflineAttendanceList;
import com.codewithben.schoolmanagementsystem.Service.OfflineReplayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/offline")
public class OfflineReplayController {

    private final OfflineReplayService offlineReplayService;

    @PostMapping("/v1/sync-attendance")
    public ResponseEntity<?> saveAttendanceRecords(@RequestHeader("staffId")String staffId,
                                                   @RequestBody List<OfflineAttendanceList> list) {

        return offlineReplayService.saveOfflineAttendanceRecords(staffId, list);
    }
}
