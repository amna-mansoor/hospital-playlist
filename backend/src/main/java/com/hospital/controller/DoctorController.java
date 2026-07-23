package com.hospital.controller;

import com.hospital.dto.DoctorInfoDto;
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

    /** List every doctor — used by the patient booking page to build a picker. */
    @GetMapping
    public List<DoctorInfoDto> listAll() {
        return doctorRepository.findAll().stream()
                .map(DoctorInfoDto::from)
                .toList();
    }

    /** A single doctor's public info — used once a patient picks one, to show name/department. */
    @GetMapping("/{id}")
    public DoctorInfoDto getOne(@PathVariable Long id) {
        return DoctorInfoDto.from(
                doctorRepository.findById(id).orElseThrow(() -> new RuntimeException("Doctor not found"))
        );
    }

    /** The logged-in doctor's own profile — used by the Doctor Dashboard to show their department. */
    @GetMapping("/me")
    public DoctorInfoDto myProfile(Authentication auth) {
        Long userId = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
        return DoctorInfoDto.from(
                doctorRepository.findByUserId(userId)
                        .orElseThrow(() -> new RuntimeException("Doctor profile not found"))
        );
    }

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