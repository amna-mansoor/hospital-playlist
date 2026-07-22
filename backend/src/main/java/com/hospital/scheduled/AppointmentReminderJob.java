package com.hospital.scheduled;

import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.repository.AppointmentRepository;
import com.hospital.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Runs automatically every hour (see the cron expression below) and emails
 * anyone whose appointment falls between 23 and 25 hours from now, so every
 * patient gets a reminder roughly one day ahead regardless of which hour
 * they booked in.
 *
 * Cron format reminder: "sec min hour day month weekday"
 * "0 0 * * * *" = run at the top of every hour.
 */
@Component
@RequiredArgsConstructor
public class AppointmentReminderJob {

    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sendReminders() {
        LocalDateTime windowStart = LocalDateTime.now().plusHours(23);
        LocalDateTime windowEnd = LocalDateTime.now().plusHours(25);

        List<Appointment> due = appointmentRepository
                .findByStatusAndReminderSentFalseAndSlotStartBetween(AppointmentStatus.BOOKED, windowStart, windowEnd);

        for (Appointment appointment : due) {
            emailService.send(
                    appointment.getPatient().getUser().getEmail(),
                    "Reminder: appointment tomorrow",
                    "This is a reminder of your appointment with Dr. " +
                            appointment.getDoctor().getUser().getFullName() +
                            " at " + appointment.getSlotStart() + "."
            );
            appointment.setReminderSent(true);
            appointmentRepository.save(appointment);
        }
    }
}
