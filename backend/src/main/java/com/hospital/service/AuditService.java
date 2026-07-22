package com.hospital.service;

import com.hospital.model.AuditLog;
import com.hospital.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(Long appointmentId, String action, String performedByEmail, String details) {
        auditLogRepository.save(AuditLog.builder()
                .appointmentId(appointmentId)
                .action(action)
                .performedByEmail(performedByEmail)
                .details(details)
                .build());
    }
}
