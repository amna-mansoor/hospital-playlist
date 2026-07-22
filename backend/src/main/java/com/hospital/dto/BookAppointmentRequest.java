package com.hospital.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookAppointmentRequest {
    @NotNull
    private Long doctorId;

    @NotNull
    private LocalDateTime slotStart; // must match an available slot exactly
}
