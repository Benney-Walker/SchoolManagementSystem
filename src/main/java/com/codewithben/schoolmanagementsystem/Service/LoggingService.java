package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Contants.LogStatus;
import com.codewithben.schoolmanagementsystem.Contants.LogType;
import com.codewithben.schoolmanagementsystem.Entity.Logs;
import com.codewithben.schoolmanagementsystem.Repository.LogsRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@Service
public class LoggingService {

    private final LogsRepository logsRepository;

    private final StaffService staffService;

    public void logActivity(LogType actionType, String actionData, String staffId, String status) {

        try {
            Logs log = new Logs();
            log.setActionDate(LocalDate.now());
            log.setActionTime(LocalTime.now());
            log.setActionType(actionType);
            log.setActionData(actionData);
            log.setCreatedBy(staffId);
            log.setStatus(LogStatus.valueOf(status));

            logsRepository.save(log);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }
}
