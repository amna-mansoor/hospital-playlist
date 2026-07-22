package com.hospital.repository;

import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Fast slot lookup - uses the idx_appt_doctor_slot index
    Optional<Appointment> findByDoctorIdAndSlotStart(Long doctorId, LocalDateTime slotStart);

    List<Appointment> findByDoctorIdAndSlotStartBetween(Long doctorId, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByPatientId(Long patientId);

    // For the 24-hour-before reminder job
    List<Appointment> findByStatusAndReminderSentFalseAndSlotStartBetween(
            AppointmentStatus status, LocalDateTime windowStart, LocalDateTime windowEnd);

    long countByDoctorIdAndSlotStartBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
}
