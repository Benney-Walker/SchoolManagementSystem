package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Logs.LogsDTO;
import com.codewithben.schoolmanagementsystem.Entity.Logs;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.LogsRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
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

    private final StaffsRepository staffsRepository;

    public void logGeneralActivity(LogType type, LogAction action, String message, String staffId, LogStatus status) {

        try {
            Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
            if (staff == null) {
                throw new RuntimeException("Could not log activity! Contact developer!");
            }
            Logs log = new Logs();
            log.setStaff(staff);
            log.setInstitution(staff.getInstitution());
            log.setActionDate(LocalDate.now());
            log.setActionTime(LocalTime.now());
            log.setType(type);
            log.setAction(action);
            log.setActionData(message);
            log.setStatus(status);

            logsRepository.save(log);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> getRecentActivity(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            logActivity(LogType.LOG, LogAction.READ,"N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        List<Logs> recentLogs = logsRepository.findByInstitution_InstitutionIdAndActionDateOrderByActionIdDesc(
                staff.getInstitution().getInstitutionId(), LocalDate.now()
        );
        if (recentLogs == null || recentLogs.isEmpty()) {
            logActivity(LogType.LOG, LogAction.READ,"N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No recent logs found"
            ));
        }

        List<LogsDTO> logList = new ArrayList<>();
        int count = 0;
        //Retrieve logs
        for (Logs log : recentLogs) {
            if (count >= 15) {
                break;
            }

            LogsDTO logsDTO = LogsDTO.builder()
                    .id(log.getActionId())
                    .time(log.getActionTime().toString())
                    .date(log.getActionDate().toString())
                    .type(log.getType().name())
                    .message(log.getActionData())
                    .status(log.getStatus().name())
                    .createdBy(log.getCreatedBy().getFirstName() +
                            " " + log.getCreatedBy().getLastName())
                    .build();

            logList.add(logsDTO);
            count++;
        }

        logActivity(LogType.LOG, LogAction.READ,"N/A", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(logList);
    }
}
