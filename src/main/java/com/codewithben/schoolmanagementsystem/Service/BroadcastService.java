package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.Constants.StaffRoles;
import com.codewithben.schoolmanagementsystem.DTO.Broadcast.SmsRequest;
import com.codewithben.schoolmanagementsystem.DTO.Broadcast.Agoo.AgooSmsResponse;
import com.codewithben.schoolmanagementsystem.Entity.*;
import com.codewithben.schoolmanagementsystem.Interface.SmsInterface;
import com.codewithben.schoolmanagementsystem.Repository.LevelRepository;
import com.codewithben.schoolmanagementsystem.Repository.MessagesRepository;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import com.codewithben.schoolmanagementsystem.Repository.StudentsRepository;
import com.codewithben.schoolmanagementsystem.Utility.UtilityClass;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastService {

    private final MessagesRepository messagesRepository;

    private final StaffsRepository staffsRepository;

    private final StudentsRepository studentsRepository;

    private final LoggingService loggingService;

    private final SmsInterface smsInterface;

    private final UtilityClass utilityClass;

    private final LevelRepository levelRepository;

    public ResponseEntity<?> broadcastCustomBulkSms(SmsRequest smsRequest, String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if (staff == null) {
            log.error("Staff with id {} not found. /Broadcasting/", staffId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<String> staffRoles = new ArrayList<>();
        List<String> levelIds = new ArrayList<>();
        for (String audience : smsRequest.getAudience()) {
            if (audience.startsWith("LV")) {
                levelIds.add(audience);
            } else {
                staffRoles.add(audience);
            }
        }

        List<Staffs> staffAudience = getStaffAudience(staffRoles, staff.getInstitution());
        List<Students> studentAudience = getStudentAudience(levelIds, staff.getInstitution());
        if (staffAudience.isEmpty() && studentAudience.isEmpty()) {
            loggingService.logGeneralActivity(
                    LogType.BROADCAST,
                    LogAction.CREATE,
                    "Selected audience has no actual recipients",
                    staffId, LogStatus.FAILED
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Selected audience has no actual recipients"
            ));
        }

        List<String> broadcastRecipients = new ArrayList<>();
        if (!staffAudience.isEmpty()) {

            for (Staffs audienceStaff : staffAudience) {
                String formattedNumber = UtilityClass.toInternational(audienceStaff.getPhoneNumber());
                if (formattedNumber == null)
                    continue;

                broadcastRecipients.add(formattedNumber);
            }
        }

        if (!studentAudience.isEmpty()) {
            for (Students audienceStudent : studentAudience) {
                String formattedNumber = UtilityClass.toInternational(audienceStudent.getParentPhoneNumber());
                if (formattedNumber == null)
                    continue;
                broadcastRecipients.add(formattedNumber);
            }
        }

        AgooSmsResponse agooSmsResponse = smsInterface.sendBulkSms(smsRequest.getMessage(), broadcastRecipients);
        if (agooSmsResponse == null) {
            loggingService.logGeneralActivity(
                    LogType.BROADCAST,
                    LogAction.CREATE,
                    "Could not send messages! Contact developers.",
                    staffId, LogStatus.FAILED
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        Messages newMessage = Messages.builder()
                .message(smsRequest.getMessage())
                .sentBy(staff)
                .audience(getAudienceList(levelIds, staffRoles))
                .audienceCount(agooSmsResponse.getData().getRecipientCount())
                .successCount(agooSmsResponse.isSuccess() ? 1 : 0)
                .failureCount(agooSmsResponse.isSuccess() ? 0 : 1)
                .build();
        messagesRepository.save(newMessage);

        loggingService.logGeneralActivity(
                LogType.BROADCAST,
                LogAction.CREATE,
                "Successfully sent messages",
                staffId, LogStatus.SUCCESS
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private List<String> getAudienceList(List<String> levelIds, List<String> staffRoles) {
        List<String> audienceList = new ArrayList<>();

        if (!levelIds.isEmpty()) {
            List<Level> levelList = levelRepository.findAllByLevelIDIn(levelIds);
            for (Level level : levelList) {
                audienceList.add(level.getLevelName());
            }
        }

        if (!staffRoles.isEmpty()) {
            audienceList.addAll(staffRoles);
        }

        return audienceList;
    }

    private List<Staffs> getStaffAudience(List<String> staffRoles, Institution institution) {

        List<Staffs> staffList = staffsRepository.findByInstitution_InstitutionId(institution.getInstitutionId());

        List<Staffs> selectedAudience = new ArrayList<>();
        for (String staffRole : staffRoles) {

            StaffRoles role;

            try {
                role = StaffRoles.valueOf(staffRole.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Unknown staff role: {}", staffRole);
                continue;
            }

            for (Staffs staff : staffList) {

                if (staff.getRoles().stream().anyMatch(roleEntity -> roleEntity.getStaffRole() == role)) {
                    if (selectedAudience.contains(staff))
                        continue;
                    selectedAudience.add(staff);
                }
            }
        }

        return selectedAudience;
    }

    private List<Students> getStudentAudience(List<String> levelIds, Institution institution) {

        List<Students> studentsList = studentsRepository
                .findAllByInstitution_InstitutionIdAndLevel_LevelIDIn(institution.getInstitutionId(), levelIds);
        if (studentsList == null || studentsList.isEmpty()) {
            return new ArrayList<>();
        }

        return utilityClass.getActiveStudents(studentsList);
    }
}
