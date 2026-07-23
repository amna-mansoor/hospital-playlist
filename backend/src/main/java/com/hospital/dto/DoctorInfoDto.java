package com.hospital.dto;

import java.time.LocalTime;

/**
 * What a patient (or anyone browsing doctors) is allowed to see about a
 * doctor — name, specialization, department, and working hours. Deliberately
 * excludes anything internal like the doctor's email or user id.
 */
public record DoctorInfoDto(
        Long id,
        String fullName,
        String specialization,
        String departmentName,
        Long departmentId,
        Integer dayOfWeek,
        LocalTime shiftStart,
        LocalTime shiftEnd,
        Integer slotLengthMinutes,
        boolean onLeave
) {
    public static DoctorInfoDto from(com.hospital.model.Doctor d) {
        return new DoctorInfoDto(
                d.getId(),
                d.getUser().getFullName(),
                d.getSpecialization(),
                d.getDepartment().getName(),
                d.getDepartment().getId(),
                d.getDayOfWeek(),
                d.getShiftStart(),
                d.getShiftEnd(),
                d.getSlotLengthMinutes(),
                d.isOnLeave()
        );
    }
}