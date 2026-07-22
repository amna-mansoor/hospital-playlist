package com.hospital.service;

import com.hospital.exception.NoBedAvailableException;
import com.hospital.model.*;
import com.hospital.repository.AdmissionRepository;
import com.hospital.repository.BedRepository;
import com.hospital.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BedService {

    private final BedRepository bedRepository;
    private final AdmissionRepository admissionRepository;
    private final PatientRepository patientRepository;
    private final EmailService emailService;

    /**
     * Auto-assigns the patient the first free bed in a department.
     *
     * Concurrency note: findAnyFreeBed uses a PESSIMISTIC_WRITE lock (see
     * BedRepository), so if two receptionists click "Admit" for two different
     * patients into the same department at the exact same millisecond, the
     * database itself serializes them - the second request simply waits until
     * the first transaction commits (bed marked occupied), then sees the next
     * free bed. Nobody can get double-assigned to the same physical bed.
     */
    @Transactional
    public Admission admitPatient(Long patientId, Long departmentId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        Bed bed = bedRepository.findAnyFreeBed(departmentId)
                .orElseThrow(() -> new NoBedAvailableException("No beds currently available in this department."));

        bed.setOccupied(true);
        bedRepository.save(bed);

        Admission admission = admissionRepository.save(Admission.builder()
                .patient(patient)
                .bed(bed)
                .status(AdmissionStatus.ADMITTED)
                .build());

        emailService.send(
                patient.getUser().getEmail(),
                "Admission Confirmed",
                "You have been admitted to bed " + bed.getBedCode() + "."
        );

        return admission;
    }

    @Transactional
    public void dischargePatient(Long admissionId) {
        Admission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new RuntimeException("Admission not found"));

        admission.setStatus(AdmissionStatus.DISCHARGED);
        admission.setDischargedAt(java.time.LocalDateTime.now());
        admissionRepository.save(admission);

        Bed bed = admission.getBed();
        bed.setOccupied(false);
        bedRepository.save(bed); // @Version guards this row too
    }

    @Transactional(readOnly = true)
    public Map<String, Object> occupancyFor(Long departmentId, String departmentName) {
        List<Bed> beds = bedRepository.findByDepartmentId(departmentId);
        long occupied = beds.stream().filter(Bed::isOccupied).count();
        return Map.of(
                "department", departmentName,
                "totalBeds", beds.size(),
                "occupied", occupied,
                "free", beds.size() - occupied
        );
    }
}
