package com.codewithben.schoolmanagementsystem.Controller;

import com.codewithben.schoolmanagementsystem.DTO.School.UpdateSchoolProfile;
import com.codewithben.schoolmanagementsystem.Service.SchoolProfileService;
import com.codewithben.schoolmanagementsystem.Utility.AuthenticatedStaffProvider;
import com.codewithben.schoolmanagementsystem.Utility.BrandingImages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * School details printed on reports. Reads are open to every staff role; changes are limited
 * to PRINCIPAL and ADMINISTRATOR in SchoolProfileService.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class SchoolProfileController {

    private final SchoolProfileService schoolProfileService;

    private final AuthenticatedStaffProvider authenticatedStaffProvider;

    @GetMapping("/v1/school-profile")
    public ResponseEntity<?> getSchoolProfile() {
        return schoolProfileService.getProfile(authenticatedStaffProvider.getStaff());
    }

    @PutMapping("/v1/school-profile")
    public ResponseEntity<?> updateSchoolProfile(@Valid @RequestBody UpdateSchoolProfile data) {
        return schoolProfileService.updateProfile(data, authenticatedStaffProvider.getStaff());
    }

    @PutMapping(value = "/v1/school-branding/{kind}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadBrandingImage(@PathVariable String kind,
                                                 @RequestParam("file") MultipartFile file) {
        BrandingImages.Kind imageKind = parseKind(kind);
        if (imageKind == null) {
            return unknownKind();
        }
        return schoolProfileService.uploadImage(imageKind, file, authenticatedStaffProvider.getStaff());
    }

    @DeleteMapping("/v1/school-branding/{kind}")
    public ResponseEntity<?> deleteBrandingImage(@PathVariable String kind) {
        BrandingImages.Kind imageKind = parseKind(kind);
        if (imageKind == null) {
            return unknownKind();
        }
        return schoolProfileService.deleteImage(imageKind, authenticatedStaffProvider.getStaff());
    }

    @GetMapping("/v1/school-branding/{kind}")
    public ResponseEntity<?> getBrandingImage(@PathVariable String kind) {
        BrandingImages.Kind imageKind = parseKind(kind);
        if (imageKind == null) {
            return unknownKind();
        }
        return schoolProfileService.getImage(imageKind, authenticatedStaffProvider.getStaff());
    }

    private static BrandingImages.Kind parseKind(String kind) {
        if (kind == null) {
            return null;
        }
        return switch (kind.trim().toLowerCase()) {
            case "logo" -> BrandingImages.Kind.LOGO;
            case "signature" -> BrandingImages.Kind.SIGNATURE;
            default -> null;
        };
    }

    private static ResponseEntity<?> unknownKind() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "message", "Unknown image type; use logo or signature"
        ));
    }
}
