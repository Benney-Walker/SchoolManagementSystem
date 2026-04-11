package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Contants.LogStatus;
import com.codewithben.schoolmanagementsystem.Contants.LogType;
import com.codewithben.schoolmanagementsystem.Entity.Logs;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.LogsRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LoggingService {

    private final LogsRepository logsRepository;

    private final UtilityClass utilityClass;

    private final StaffsRepository staffsRepository;

    public LoggingService(LogsRepository logsRepository, UtilityClass utilityClass, StaffsRepository staffsRepository) {
        this.logsRepository = logsRepository;
        this.utilityClass = utilityClass;
        this.staffsRepository = staffsRepository;
    }

    public void logActivity(String actionType, String actionData, String staffId, String status) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);

        Logs log = new Logs();

        log.setActionId(utilityClass.generateEntityId("LOG"));
        log.setActionTime(LocalDateTime.now());
        log.setActionType(LogType.valueOf(actionType));
        log.setActionData(actionData);
        log.setCreatedBy(staff);
        log.setStatus(LogStatus.valueOf(status));

        logsRepository.save(log);
    }
}
