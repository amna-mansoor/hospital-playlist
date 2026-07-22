package com.hospital.repository;

import com.hospital.model.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    List<Waitlist> findByDoctorIdAndRequestedDateAndNotifiedFalseOrderByCreatedAtAsc(
            Long doctorId, LocalDateTime requestedDate);
}
