package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Constants.LogAction;
import com.codewithben.schoolmanagementsystem.Constants.LogStatus;
import com.codewithben.schoolmanagementsystem.Constants.LogType;
import com.codewithben.schoolmanagementsystem.Constants.StaffRoles;
import com.codewithben.schoolmanagementsystem.DTO.School.SchoolProfileDTO;
import com.codewithben.schoolmanagementsystem.DTO.School.UpdateSchoolProfile;
import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Entity.Staffs;
import com.codewithben.schoolmanagementsystem.Repository.InstitutiionRepository;
import com.codewithben.schoolmanagementsystem.Utility.BrandingImages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The school details printed on reports: name (already on the institution), motto, address,
 * logo and principal's signature.
 *
 * Any staff member may read the profile; only PRINCIPAL and ADMINISTRATOR may change it.
 * The /api/admin prefix admits every staff role, so the role is checked here.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolProfileService {

    private static final Set<StaffRoles> EDITORS = Set.of(StaffRoles.PRINCIPAL, StaffRoles.ADMINISTRATOR);

    private final InstitutiionRepository institutionRepository;
    private final ObjectStorageService storage;
    private final BrandingAssetService brandingAssetService;
    private final LoggingService loggingService;

    public ResponseEntity<?> getProfile(Staffs staff) {
        if (staff == null || staff.getInstitution() == null) {
            return staffNotFound();
        }
        return ResponseEntity.ok(toDto(staff.getInstitution()));
    }

    public ResponseEntity<?> updateProfile(UpdateSchoolProfile data, Staffs staff) {
        if (staff == null || staff.getInstitution() == null) {
            return staffNotFound();
        }
        if (!canEdit(staff)) {
            return forbidden(staff, "update the school profile");
        }

        Institution institution = staff.getInstitution();
        institution.setMotto(blankToNull(data.getMotto()));
        institution.setAddress(blankToNull(data.getAddress()));
        institutionRepository.save(institution);

        loggingService.logGeneralActivity(LogType.INSTITUTION, LogAction.UPDATE,
                "Updated school motto and address", staff.getStaffId(), LogStatus.SUCCESS);
        return ResponseEntity.ok(toDto(institution));
    }

    public ResponseEntity<?> uploadImage(BrandingImages.Kind kind, MultipartFile file, Staffs staff) {
        if (staff == null || staff.getInstitution() == null) {
            return staffNotFound();
        }
        if (!canEdit(staff)) {
            return forbidden(staff, "upload the school " + kind.label());
        }
        if (!storage.isConfigured()) {
            return storageNotConfigured();
        }

        byte[] png;
        try {
            png = BrandingImages.prepare(file == null ? null : file.getBytes(), kind);
        } catch (BrandingImages.InvalidImageException e) {
            loggingService.logGeneralActivity(LogType.INSTITUTION, LogAction.UPDATE,
                    "Rejected school " + kind.label() + " upload: " + e.getMessage(),
                    staff.getStaffId(), LogStatus.FAILED);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            log.error("Could not read uploaded {} for {}", kind.label(), staff.getStaffId(), e);
            return ResponseEntity.badRequest().body(Map.of("message", "Could not read the uploaded file"));
        }

        Institution institution = staff.getInstitution();
        String key = "institutions/" + institution.getInstitutionId() + "/branding/"
                + kind.label() + "-" + UUID.randomUUID() + ".png";

        String newUrl;
        try {
            newUrl = storage.put(key, png, MediaType.IMAGE_PNG_VALUE);
        } catch (RuntimeException e) {
            log.error("Could not store school {} for institution {}", kind.label(), institution.getInstitutionId(), e);
            loggingService.logGeneralActivity(LogType.INSTITUTION, LogAction.UPDATE,
                    "Could not store school " + kind.label(), staff.getStaffId(), LogStatus.FAILED);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                    "message", "Could not save the " + kind.label() + ". Try again later"
            ));
        }

        String oldUrl = currentUrl(institution, kind);
        setUrl(institution, kind, newUrl);
        try {
            institutionRepository.save(institution);
        } catch (RuntimeException e) {
            // Don't leave an unreferenced file behind if the URL couldn't be saved.
            deleteQuietly(newUrl);
            throw e;
        }

        BufferedImage stored = BrandingImages.decode(png);
        if (stored != null) {
            brandingAssetService.put(newUrl, stored);
        }
        if (oldUrl != null) {
            brandingAssetService.evict(oldUrl);
            deleteQuietly(oldUrl);
        }

        loggingService.logGeneralActivity(LogType.INSTITUTION, LogAction.UPDATE,
                "Uploaded school " + kind.label(), staff.getStaffId(), LogStatus.SUCCESS);
        return ResponseEntity.ok(toDto(institution));
    }

    public ResponseEntity<?> deleteImage(BrandingImages.Kind kind, Staffs staff) {
        if (staff == null || staff.getInstitution() == null) {
            return staffNotFound();
        }
        if (!canEdit(staff)) {
            return forbidden(staff, "remove the school " + kind.label());
        }

        Institution institution = staff.getInstitution();
        String oldUrl = currentUrl(institution, kind);
        if (oldUrl == null) {
            return ResponseEntity.ok(toDto(institution));
        }

        setUrl(institution, kind, null);
        institutionRepository.save(institution);
        brandingAssetService.evict(oldUrl);
        deleteQuietly(oldUrl);

        loggingService.logGeneralActivity(LogType.INSTITUTION, LogAction.DELETE,
                "Removed school " + kind.label(), staff.getStaffId(), LogStatus.SUCCESS);
        return ResponseEntity.ok(toDto(institution));
    }

    /** The stored image as PNG, for previews in the desktop app. */
    public ResponseEntity<?> getImage(BrandingImages.Kind kind, Staffs staff) {
        if (staff == null || staff.getInstitution() == null) {
            return staffNotFound();
        }
        String url = currentUrl(staff.getInstitution(), kind);
        return brandingAssetService.getImage(url)
                .<ResponseEntity<?>>map(image -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(BrandingImages.toPng(image)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "message", "No " + kind.label() + " uploaded"
                )));
    }

    private SchoolProfileDTO toDto(Institution institution) {
        return SchoolProfileDTO.builder()
                .schoolName(institution.getInstitutionName())
                .motto(institution.getMotto())
                .address(institution.getAddress())
                .hasLogo(institution.getReportLogoUrl() != null)
                .hasSignature(institution.getReportSignatureUrl() != null)
                .uploadsEnabled(storage.isConfigured())
                .build();
    }

    private static boolean canEdit(Staffs staff) {
        return staff.getRoles() != null
                && staff.getRoles().stream().anyMatch(role -> EDITORS.contains(role.getStaffRole()));
    }

    private static String currentUrl(Institution institution, BrandingImages.Kind kind) {
        return kind == BrandingImages.Kind.LOGO ? institution.getReportLogoUrl() : institution.getReportSignatureUrl();
    }

    private static void setUrl(Institution institution, BrandingImages.Kind kind, String url) {
        if (kind == BrandingImages.Kind.LOGO) {
            institution.setReportLogoUrl(url);
        } else {
            institution.setReportSignatureUrl(url);
        }
    }

    private void deleteQuietly(String url) {
        try {
            storage.delete(url);
        } catch (RuntimeException e) {
            log.warn("Could not delete old branding file {}; it can be removed from the bucket by hand", url, e);
        }
    }

    private ResponseEntity<?> forbidden(Staffs staff, String action) {
        loggingService.logGeneralActivity(LogType.INSTITUTION, LogAction.UPDATE,
                "Not allowed to " + action, staff.getStaffId(), LogStatus.FAILED);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "message", "Only the principal or an administrator can " + action
        ));
    }

    private static ResponseEntity<?> staffNotFound() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "message", "Session expired. Login again"
        ));
    }

    private static ResponseEntity<?> storageNotConfigured() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "message", "File storage is not configured. Contact support"
        ));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
