package com.hospital.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Every cancellation / reschedule writes a row here so admins can answer
 * "who changed this appointment, and when, and what did it look like before?"
 */
@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long appointmentId;

    private String action; // "CREATED", "CANCELLED", "RESCHEDULED", "COMPLETED", "NO_SHOW"

    private String performedByEmail;

    @Column(columnDefinition = "TEXT")
    private String details; // free-text JSON snapshot of before/after

    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        timestamp = LocalDateTime.now();
    }
}
