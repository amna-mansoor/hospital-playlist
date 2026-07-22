package com.hospital.controller;

import com.hospital.model.Appointment;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @GetMapping("/dashboard/today")
    public List<Appointment> todaysSchedule(Authentication auth) {
        Long userId = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
        var doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found"));
        LocalDate today = LocalDate.now();
        return appointmentRepository.findByDoctorIdAndSlotStartBetween(
                doctor.getId(), today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    @GetMapping("/dashboard/history/{patientId}")
    public List<Appointment> patientHistory(@PathVariable Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }
}
