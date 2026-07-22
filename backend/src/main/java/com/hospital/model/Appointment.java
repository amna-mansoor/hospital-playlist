package com.hospital.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * THE MOST IMPORTANT ENTITY FOR CONCURRENCY SAFETY.
 *
 * The @Version field below is what Spring/JPA (via Hibernate) uses for
 * "optimistic locking". Here is the plain-English version of how it works:
 *
 * 1. Every row in the appointments table has a hidden number column called "version".
 * 2. When Patient A loads a slot to book it, they also silently load version = 0.
 * 3. When Patient B loads the SAME slot half a second later, they also get version = 0.
 * 4. Patient A submits the booking first. The database saves it and bumps the
 *    row to version = 1.
 * 5. Patient B submits next. The database checks: "the request expects version 0,
 *    but the row is already at version 1" -> it REJECTS the update and throws an
 *    OptimisticLockException.
 * 6. Our service layer catches that exception and tells Patient B: "Sorry, that
 *    slot was just taken, please pick another one" instead of silently creating
 *    a double-booking.
 *
 * This is much cheaper than locking the whole table (pessimistic locking) and
 * is the standard, interview-friendly way to solve this problem.
 *
 * We ALSO add a unique constraint on (doctor_id, slot_start) as a second,
 * database-level safety net -- belt and suspenders.
 */
@Entity
@Table(
    name = "appointments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id", "slot_start"}),
    indexes = {
        @Index(name = "idx_appt_doctor_slot", columnList = "doctor_id, slot_start"),
        @Index(name = "idx_appt_patient", columnList = "patient_id")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDateTime slotStart;

    @Column(nullable = false)
    private LocalDateTime slotEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.BOOKED;

    @Column(nullable = false)
    @Builder.Default
    private boolean reminderSent = false;

    // === THE OPTIMISTIC LOCK ===
    @Version
    private Long version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
