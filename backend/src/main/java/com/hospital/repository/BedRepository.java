package com.hospital.repository;

import com.hospital.model.Bed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface BedRepository extends JpaRepository<Bed, Long> {

    List<Bed> findByDepartmentId(Long departmentId);

    long countByDepartmentIdAndOccupiedTrue(Long departmentId);

    // PESSIMISTIC_WRITE here means: "lock this exact row in the database until my
    // transaction finishes". We use this (instead of optimistic locking) for bed
    // assignment because beds are a scarcer, more contended resource and we want
    // the *first* request to simply wait its turn rather than fail and retry.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bed b where b.department.id = :departmentId and b.occupied = false order by b.id asc")
    List<Bed> findFirstFreeBedForUpdate(Long departmentId);

    default Optional<Bed> findAnyFreeBed(Long departmentId) {
        List<Bed> beds = findFirstFreeBedForUpdate(departmentId);
        return beds.isEmpty() ? Optional.empty() : Optional.of(beds.get(0));
    }
}
