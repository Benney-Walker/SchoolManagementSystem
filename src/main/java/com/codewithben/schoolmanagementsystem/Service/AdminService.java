package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Contants.LogStatus;
import com.codewithben.schoolmanagementsystem.Contants.LogType;
import com.codewithben.schoolmanagementsystem.DTO.Institution.LogsDTO;
import com.codewithben.schoolmanagementsystem.Entity.Logs;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.LogsRepository;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {
    private final LogsRepository logsRepository;

    private final UtilityClass utilityClass;

    public AdminService(LogsRepository logsRepository, UtilityClass utilityClass) {
        this.logsRepository = logsRepository;
        this.utilityClass = utilityClass;
    }

    public void logSystemActivities(LogType logType, LogStatus logStatus, String message, Staffs createdBy) {
        try {
            Logs logs = new Logs();
            logs.setActionId(utilityClass.generateEntityId("LOG"));
            logs.setActionDate(LocalDate.now());
            logs.setActionTime(LocalTime.now());
            logs.setActionType(logType);
            logs.setActionData(message);
            logs.setCreatedBy(createdBy);
            logs.setStatus(logStatus);
            logsRepository.save(logs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<LogsDTO> findLogsByDate(LocalDate date) {
        List<Logs> logs = logsRepository.findByActionDate(date);
        if (logs == null || logs.isEmpty()) {
            return new ArrayList<>();
        }

        List<LogsDTO> systemLogs = new ArrayList<>();
        for (Logs log : logs) {
            LogsDTO logsDTO = new LogsDTO();
            logsDTO.setId(log.getActionId());
            logsDTO.setDate(log.getActionDate().toString());
            logsDTO.setTime(log.getActionTime().toString());
            logsDTO.setType(log.getActionType().toString());
            logsDTO.setMessage(log.getActionData());
            logsDTO.setCreatedBy(log.getCreatedBy().getFirstName() + " " + log.getCreatedBy().getLastName());
            logsDTO.setStatus(log.getStatus().toString());

            systemLogs.add(logsDTO);
        }

        return systemLogs;
    }
}
