package com.codewithben.schoolmanagementsystem.DTO.School;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Motto and address printed on reports. Empty or null clears the value. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateSchoolProfile {

    @Size(max = 200, message = "Motto must be 200 characters or fewer")
    private String motto;

    @Size(max = 255, message = "Address must be 255 characters or fewer")
    private String address;
}
