package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Contants.LogStatus;
import com.codewithben.schoolmanagementsystem.Contants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Logs.LogsDTO;
import com.codewithben.schoolmanagementsystem.Entity.Logs;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.LogsRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class LoggingService {

    private final LogsRepository logsRepository;

    private final StaffService staffService;

    public void logActivity(LogType actionType, String actionData, String staffId, String status) {

        Staffs staff = staffService.getStaffDetails(staffId);
        if (staff == null) {
            return;
        }

        try {
            Logs log = new Logs();
            log.setActionDate(LocalDate.now());
            log.setActionTime(LocalTime.now());
            log.setActionType(actionType);
            log.setActionData(actionData);
            log.setCreatedBy(staff);
            log.setInstitution(staff.getInstitution());
            log.setStatus(LogStatus.valueOf(status));

            logsRepository.save(log);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> getRecentActivity(String staffId) {

        Staffs staff = staffService.getStaffDetails(staffId);
        if (staff == null) {
            logActivity(LogType.LOGS, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        List<Logs> recentLogs = logsRepository.findByInstitution_InstitutionId(staff.getInstitution().getInstitutionId());
        if (recentLogs == null || recentLogs.isEmpty()) {
            logActivity(LogType.LOGS, "N/A", staffId, "FAILED");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No recent logs found"
            ));
        }

        List<LogsDTO> logList = new ArrayList<>();
        int count = 0;
        //Retrieve logs
        for (Logs log : recentLogs) {
            if (count >= 10) {
                break;
            }

            LogsDTO logsDTO = LogsDTO.builder()
                    .id(log.getActionId())
                    .time(log.getActionTime().toString())
                    .date(log.getActionDate().toString())
                    .type(log.getActionType().name())
                    .message(log.getActionData())
                    .status(log.getStatus().name())
                    .createdBy(log.getCreatedBy().getFirstName() +
                            " " + log.getCreatedBy().getLastName())
                    .build();

            logList.add(logsDTO);
            count++;
        }

        return ResponseEntity.ok(logList);
    }
}
