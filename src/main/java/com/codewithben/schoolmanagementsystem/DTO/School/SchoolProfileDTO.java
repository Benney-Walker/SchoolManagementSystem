package com.codewithben.schoolmanagementsystem.DTO.School;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchoolProfileDTO {
    private String schoolName;
    private String motto;
    private String address;
    private boolean hasLogo;
    private boolean hasSignature;
    // False while the storage bucket isn't configured: uploads will be refused.
    private boolean uploadsEnabled;
}
