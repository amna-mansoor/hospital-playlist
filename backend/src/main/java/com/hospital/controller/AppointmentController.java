package com.hospital.controller;

import com.hospital.dto.BookAppointmentRequest;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.repository.UserRepository;
import com.hospital.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserRepository userRepository;

    @GetMapping("/slots")
    public List<AppointmentService.SlotView> getSlots(@RequestParam Long doctorId,
                                                        @RequestParam String date) {
        return appointmentService.getAvailableSlots(doctorId, LocalDate.parse(date));
    }

    @PostMapping("/book")
    public Appointment book(@Valid @RequestBody BookAppointmentRequest request, Authentication auth) {
        Long userId = currentUserId(auth);
        return appointmentService.bookAppointment(userId, request);
    }

    @PostMapping("/{id}/cancel")
    public void cancel(@PathVariable Long id, Authentication auth) {
        appointmentService.cancelAppointment(id, auth.getName());
    }

    @PostMapping("/{id}/status")
    public Appointment markStatus(@PathVariable Long id, @RequestParam AppointmentStatus status, Authentication auth) {
        return appointmentService.markStatus(id, status, auth.getName());
    }

    private Long currentUserId(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}
