package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.Constants.SpecialPaymentType;
import com.codewithben.schoolmanagementsystem.DTO.SpecialPayments.NewSpecialPayment;
import com.codewithben.schoolmanagementsystem.DTO.SpecialPayments.SpecialPaymentRecord;
import com.codewithben.schoolmanagementsystem.DTO.SpecialPayments.UpdateSpecialPayment;
import com.codewithben.schoolmanagementsystem.Entity.Level;
import com.codewithben.schoolmanagementsystem.Entity.LevelSpecialPayment;
import com.codewithben.schoolmanagementsystem.Entity.SpecialPayment;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.LevelRepository;
import com.codewithben.schoolmanagementsystem.Repository.LevelSpecialPaymentRepo;
import com.codewithben.schoolmanagementsystem.Repository.SpecialPaymentRepo;
import com.codewithben.schoolmanagementsystem.Repository.StaffsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class SpecialPaymentService {

    private final SpecialPaymentRepo specialPaymentRepo;

    private final LevelSpecialPaymentRepo levelSpecialPaymentRepo;

    private final LevelRepository levelRepository;

    private final StaffsRepository staffsRepository;

    private final LoggingService loggingService;

    public ResponseEntity<?> addSpecialPayment(NewSpecialPayment newSpecialPayment, String staffId) {

        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if(staff == null){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        SpecialPayment specialPayment = specialPaymentRepo.findByInstitution_InstitutionIdAndPaymentType(
                staff.getInstitution().getInstitutionId(),
                SpecialPaymentType.valueOf(newSpecialPayment.getPaymentType().toUpperCase())
        ).orElse(null);
        if(specialPayment == null){
            specialPayment = new SpecialPayment();
            specialPayment.setPaymentType(SpecialPaymentType.valueOf(newSpecialPayment.getPaymentType().toUpperCase()));
            specialPayment.setInstitution(staff.getInstitution());
            specialPaymentRepo.save(specialPayment);
        }

        List<String> skippedLevels = new ArrayList<>();
        boolean anyInvalidId = false;

        for (String levelId : newSpecialPayment.getLevelList()) {
            Level level = levelRepository.findByLevelID(levelId).orElse(null);
            if(level == null){
                anyInvalidId = true;
                break;
            }

            LevelSpecialPayment levelSpecialPayment = levelSpecialPaymentRepo
                    .findBySpecialPayment_IdAndLevel_LevelID(specialPayment.getId(), levelId).orElse(null);
            if (levelSpecialPayment != null) {
                skippedLevels.add(level.getLevelName());
                continue;
            }

            levelSpecialPayment = new LevelSpecialPayment();
            levelSpecialPayment.setSpecialPayment(specialPayment);
            levelSpecialPayment.setLevel(level);
            levelSpecialPayment.setAmount(newSpecialPayment.getAmount());
            levelSpecialPayment.setDescription(newSpecialPayment.getDescription());
            levelSpecialPayment.setCreatedAt(LocalDate.now());
            levelSpecialPaymentRepo.save(levelSpecialPayment);
        }

        if(anyInvalidId) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message", "No record saved, some class Ids invalid"
            ));
        }

        String message;
        if (skippedLevels.isEmpty()) {
            message = "All records saved";
        } else {
            message = "Records for " + skippedLevels.stream().toList() + " already existed. Update if necessary";
        }

        loggingService.logGeneralActivity(
                LogType.SPECIAL_FEES,
                LogAction.CREATE,
                message,
                staffId,
                LogStatus.SUCCESS
        );
        return ResponseEntity.ok(Map.of(
                "message", message
        ));
    }

    public ResponseEntity<?> loadSpecialPayments(String paymentType, String staffId) {
        Staffs staff = staffsRepository.findByStaffId(staffId).orElse(null);
        if(staff == null){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        List<LevelSpecialPayment> specialPaymentList = levelSpecialPaymentRepo
                .findBySpecialPayment_PaymentTypeAndSpecialPayment_Institution(
                        SpecialPaymentType.valueOf(paymentType.toUpperCase()),
                        staff.getInstitution()
                );
        if (specialPaymentList == null || specialPaymentList.isEmpty()) {
            loggingService.logGeneralActivity(
                    LogType.SPECIAL_FEES,
                    LogAction.READ,
                    "No special payment of type " + paymentType,
                    staffId,
                    LogStatus.FAILED
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "No special payment of type " + paymentType
            ));
        }

        List<SpecialPaymentRecord> specialPaymentRecordList = new ArrayList<>();

        for (LevelSpecialPayment specialPayment : specialPaymentList) {
            SpecialPaymentRecord newRecord = SpecialPaymentRecord.builder()
                    .id(specialPayment.getId())
                    .type(specialPayment.getSpecialPayment().getPaymentType().name())
                    .amount(specialPayment.getAmount())
                    .levelName(specialPayment.getLevel().getLevelName())
                    .description(specialPayment.getDescription())
                    .createdAt(specialPayment.getCreatedAt().toString())
                    .build();
            specialPaymentRecordList.add(newRecord);
        }

        return ResponseEntity.ok(specialPaymentRecordList);
    }

    public ResponseEntity<?> updateSpecialPayment(UpdateSpecialPayment updateSpecialPayment, String staffId) {

        LevelSpecialPayment levelSpecialPayment = levelSpecialPaymentRepo.findById(updateSpecialPayment.getId()).orElse(null);
        if(levelSpecialPayment == null){
            loggingService.logGeneralActivity(
                    LogType.SPECIAL_FEES,
                    LogAction.UPDATE,
                    "Record do not exist",
                    staffId,
                    LogStatus.FAILED
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Record not exist"
            ));
        }

        levelSpecialPayment.setAmount(updateSpecialPayment.getAmount());
        levelSpecialPayment.setDescription(updateSpecialPayment.getDescription());
        levelSpecialPayment.setUpdatedAt(LocalDate.now());
        levelSpecialPaymentRepo.save(levelSpecialPayment);

        loggingService.logGeneralActivity(
                LogType.SPECIAL_FEES,
                LogAction.UPDATE,
                "Successfully updated record",
                staffId,
                LogStatus.SUCCESS
        );
        return ResponseEntity.ok().build();
    }
}
