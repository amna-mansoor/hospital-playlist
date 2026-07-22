package com.hospital.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "beds", indexes = @Index(name = "idx_bed_department", columnList = "department_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false, unique = true)
    private String bedCode; // e.g. "ICU-04"

    @Column(nullable = false)
    @Builder.Default
    private boolean occupied = false;

    // Optimistic lock here too: two receptionists could try to assign the
    // same free bed to two different patients at the same moment.
    @Version
    private Long version;
}
