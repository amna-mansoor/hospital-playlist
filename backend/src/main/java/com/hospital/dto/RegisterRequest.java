package com.hospital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String fullName;

    @Email @NotBlank
    private String email;

    @NotBlank
    private String password;

    // "PATIENT" or "DOCTOR" (admins are created manually/seeded, not self-registered)
    @NotBlank
    private String role;
}
