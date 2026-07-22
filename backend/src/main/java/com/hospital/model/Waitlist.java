package com.hospital.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "waitlist", indexes = @Index(name = "idx_waitlist_doctor_date", columnList = "doctor_id, requested_date"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "requested_date", nullable = false)
    private LocalDateTime requestedDate; // the day the patient wants an appointment

    @Column(nullable = false)
    @Builder.Default
    private boolean notified = false;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
