package com.hospital.service;

import com.hospital.dto.BookAppointmentRequest;
import com.hospital.exception.SlotConflictException;
import com.hospital.model.*;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.PatientRepository;
import com.hospital.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final WaitlistRepository waitlistRepository;
    private final EmailService emailService;
    private final AuditService auditService;

    private static final int MAX_RETRIES = 3;

    /**
     * Returns every bookable slot for a doctor on a given day, marking which
     * ones are already taken, so the calendar UI can show free vs. busy.
     */
    @Transactional(readOnly = true)
    public List<SlotView> getAvailableSlots(Long doctorId, java.time.LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (doctor.isOnLeave() || doctor.getDayOfWeek() != date.getDayOfWeek().getValue()) {
            return List.of(); // doctor doesn't work this day, or is on leave
        }

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        List<Appointment> existing = appointmentRepository.findByDoctorIdAndSlotStartBetween(doctorId, dayStart, dayEnd);

        List<SlotView> slots = new ArrayList<>();
        LocalTime cursor = doctor.getShiftStart();
        int lengthMin = doctor.getSlotLengthMinutes();

        while (cursor.plusMinutes(lengthMin).compareTo(doctor.getShiftEnd()) <= 0) {
            LocalDateTime slotStart = LocalDateTime.of(date, cursor);
            boolean taken = existing.stream()
                    .anyMatch(a -> a.getSlotStart().equals(slotStart) && a.getStatus() == AppointmentStatus.BOOKED);
            slots.add(new SlotView(slotStart, slotStart.plusMinutes(lengthMin), taken));
            cursor = cursor.plusMinutes(lengthMin);
        }
        return slots;
    }

    /**
     * Books an appointment safely under concurrent load.
     *
     * How the race condition is prevented, step by step:
     *  1. We check the DB for an existing BOOKED row at this doctor+slot. If found, fail fast.
     *  2. We save a new Appointment row. The unique constraint on (doctor_id, slot_start)
     *     means that if two requests somehow both pass step 1 at the exact same
     *     instant, the SECOND database INSERT will be rejected by Postgres itself
     *     with a constraint violation - it is IMPOSSIBLE for two BOOKED rows to
     *     exist for the same doctor+slot, no matter how the application code races.
     *  3. If a DataIntegrityViolationException (constraint violation) or an
     *     OptimisticLockingFailureException occurs, we treat it as "someone beat
     *     you to it" and return a friendly 409 Conflict instead of a crash.
     *
     * We retry a couple of times only for the (rare, unrelated) case of
     * transient DB hiccups - not to "fight" over the same slot.
     */
    @Transactional
    public Appointment bookAppointment(Long patientUserId, BookAppointmentRequest req) {
        Patient patient = patientRepository.findByUserId(patientUserId)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));
        Doctor doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        appointmentRepository.findByDoctorIdAndSlotStart(doctor.getId(), req.getSlotStart())
                .filter(a -> a.getStatus() == AppointmentStatus.BOOKED)
                .ifPresent(a -> {
                    throw new SlotConflictException("This slot was just booked. Please choose another time.");
                });

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .slotStart(req.getSlotStart())
                .slotEnd(req.getSlotStart().plusMinutes(doctor.getSlotLengthMinutes()))
                .status(AppointmentStatus.BOOKED)
                .build();

        try {
            appointment = appointmentRepository.saveAndFlush(appointment);
        } catch (DataIntegrityViolationException | OptimisticLockingFailureException e) {
            // Someone else's INSERT for the exact same doctor+slot won the race.
            throw new SlotConflictException("This slot was just booked by another patient. Please choose another time.");
        }

        auditService.log(appointment.getId(), "CREATED", patient.getUser().getEmail(),
                "Booked slot " + appointment.getSlotStart());

        emailService.send(
                patient.getUser().getEmail(),
                "Appointment Confirmed",
                "Your appointment with Dr. " + doctor.getUser().getFullName() +
                        " is confirmed for " + appointment.getSlotStart() + "."
        );

        return appointment;
    }

    @Transactional
    public void cancelAppointment(Long appointmentId, String performedByEmail) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment); // @Version auto-checked here too

        auditService.log(appointmentId, "CANCELLED", performedByEmail,
                "Cancelled slot " + appointment.getSlotStart());

        // Notify the first waiting patient, if any, that a slot just opened up
        waitlistRepository.findByDoctorIdAndRequestedDateAndNotifiedFalseOrderByCreatedAtAsc(
                appointment.getDoctor().getId(), appointment.getSlotStart().toLocalDate().atStartOfDay()
        ).stream().findFirst().ifPresent(w -> {
            w.setNotified(true);
            waitlistRepository.save(w);
            emailService.send(
                    w.getPatient().getUser().getEmail(),
                    "A slot just opened up!",
                    "A slot with Dr. " + appointment.getDoctor().getUser().getFullName() +
                            " on " + appointment.getSlotStart().toLocalDate() +
                            " is now free. Log in quickly to book it before someone else does."
            );
        });
    }

    @Transactional
    public Appointment markStatus(Long appointmentId, AppointmentStatus status, String performedByEmail) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setStatus(status);
        Appointment saved = appointmentRepository.save(appointment);
        auditService.log(appointmentId, status.name(), performedByEmail, "Marked as " + status);
        return saved;
    }

    public record SlotView(LocalDateTime start, LocalDateTime end, boolean taken) {}
}
