package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Logs.LogsDTO;
import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Entity.Logs;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.LogsRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);

    private final LogsRepository logsRepository;

    private final StaffsRepository staffsRepository;

    public void logGeneralActivity(LogType type, LogAction action, String message, String staffId, LogStatus status) {

        try {
            Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
            if (staff == null) {
                logger.warn("Skipped audit log ({} {}): no staff found for staffId='{}'", type, action, staffId);
                return;
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
        } catch (Exception e) {
            // Audit logging is best-effort: log the failure, never propagate it.
            logger.error("Failed to write audit log ({} {}) for staffId='{}'", type, action, staffId, e);
        }
    }

    public void logNewSubscription(LogType type, LogAction action, String message, LogStatus status, Institution institution) {
        try {
            Logs log = new Logs();
            log.setStaff(null);
            log.setInstitution(institution);
            log.setActionDate(LocalDate.now());
            log.setActionTime(LocalTime.now());
            log.setType(type);
            log.setAction(action);
            log.setActionData(message);
            log.setStatus(status);

            logsRepository.save(log);
        } catch (Exception e) {
            // Best-effort: a failed audit write must not break onboarding.
            logger.error("Failed to write subscription audit log ({} {})", type, action, e);
        }
    }

    public ResponseEntity<?> getRecentActivity(String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            logGeneralActivity(LogType.LOG, LogAction.READ,"N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        List<Logs> recentLogs = logsRepository.findFirst15ByInstitution_InstitutionIdAndActionDateOrderByActionIdDesc(
                staff.getInstitution().getInstitutionId(), LocalDate.now()
        );
        if (recentLogs == null || recentLogs.isEmpty()) {
            logGeneralActivity(LogType.LOG, LogAction.READ,"N/A", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No recent logs found"
            ));
        }

        List<LogsDTO> logList = new ArrayList<>();
        //Retrieve logs
        for (Logs log : recentLogs) {

            LogsDTO logsDTO = LogsDTO.builder()
                    .id(log.getActionId())
                    .time(log.getActionTime().toString())
                    .date(log.getActionDate().toString())
                    .type(log.getType().name())
                    .message(log.getActionData())
                    .status(log.getStatus().name())
                    .createdBy(resolveCreatedBy(log))
                    .build();

            logList.add(logsDTO);
        }

        logGeneralActivity(LogType.LOG, LogAction.READ,"Fetched recent activities", staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(logList);
    }

    public ResponseEntity<?> getStaffLogsBetween(String staffId, String selectedStaff, LocalDate from, LocalDate to) {

        Staffs staff = staffsRepository.findByStaffId(selectedStaff).orElse(null);
        if (staff == null) {
            logGeneralActivity(LogType.LOG, LogAction.READ,"Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        List<Logs> staffLogsList = logsRepository
                .findByStaff_StaffIdAndActionDateBetweenOrderByActionIdDesc(
                        selectedStaff, from, to
                );

        if (staffLogsList == null || staffLogsList.isEmpty()) {
            logGeneralActivity(LogType.LOG, LogAction.READ,"No logs exits for this period", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No logs exits for this period"
            ));
        }

        List<LogsDTO> logsList = new ArrayList<>();
        for (Logs log : staffLogsList) {

            LogsDTO staffLog = LogsDTO.builder()
                    .id(log.getActionId())
                    .time(log.getActionTime().toString())
                    .date(log.getActionDate().toString())
                    .type(log.getType().name())
                    .message(log.getActionData())
                    .status(log.getStatus().name())
                    .createdBy(resolveCreatedBy(log))
                    .build();
            logsList.add(staffLog);
        }

        logGeneralActivity(
                LogType.LOG, LogAction.READ,
                "Fetched logs for " + staff.getFirstName() + " " + staff.getLastName() + " between " + from.toString() + " and " + to.toString(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(logsList);
    }

    public ResponseEntity<?> getLogsBetween(String staffId, LocalDate from, LocalDate to) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            logGeneralActivity(LogType.LOG, LogAction.READ,"Invalid staff Id", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Invalid staff Id"
            ));
        }

        List<Logs> logsList = logsRepository.findByInstitution_InstitutionIdAndActionDateBetweenOrderByActionIdDesc(
                staff.getInstitution().getInstitutionId(), from, to
        );

        if (logsList == null || logsList.isEmpty()) {
            logGeneralActivity(LogType.LOG, LogAction.READ,"No logs found", staffId, LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No logs found"
            ));
        }

        List<LogsDTO> logList = new ArrayList<>();
        //Retrieve logs
        for (Logs log : logsList) {

            LogsDTO logsDTO = LogsDTO.builder()
                    .id(log.getActionId())
                    .time(log.getActionTime().toString())
                    .date(log.getActionDate().toString())
                    .type(log.getType().name())
                    .message(log.getActionData())
                    .status(log.getStatus().name())
                    .createdBy(resolveCreatedBy(log))
                    .build();

            logList.add(logsDTO);
        }

        logGeneralActivity(
                LogType.LOG, LogAction.READ,
                "Fetched logs between " + from.toString() + " and " + to.toString(),
                staffId, LogStatus.SUCCESS);
        return ResponseEntity.ok(logList);
    }

    private String resolveCreatedBy(Logs log) {
        Staffs staff = log.getStaff();
        if (staff == null) {
            return "System";
        }
        return staff.getFirstName() + " " + staff.getLastName();
    }


}