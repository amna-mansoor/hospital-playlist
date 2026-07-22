package com.hospital.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "doctors")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    private String specialization;

    @Column(nullable = false)
    private Integer dayOfWeek; // 1=Monday .. 7=Sunday

    @Column(nullable = false)
    private LocalTime shiftStart;

    @Column(nullable = false)
    private LocalTime shiftEnd;

    @Column(nullable = false)
    @Builder.Default
    private Integer slotLengthMinutes = 30;

    @Column(nullable = false)
    @Builder.Default
    private boolean onLeave = false;
}
