package com.hospital.controller;

import com.hospital.dto.AdmitPatientRequest;
import com.hospital.model.Admission;
import com.hospital.service.BedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/beds")
@RequiredArgsConstructor
public class BedController {

    private final BedService bedService;

    @PostMapping("/admit")
    public Admission admit(@Valid @RequestBody AdmitPatientRequest request) {
        return bedService.admitPatient(request.getPatientId(), request.getDepartmentId());
    }

    @PostMapping("/discharge/{admissionId}")
    public void discharge(@PathVariable Long admissionId) {
        bedService.dischargePatient(admissionId);
    }

    @GetMapping("/occupancy/{departmentId}")
    public Map<String, Object> occupancy(@PathVariable Long departmentId, @RequestParam String departmentName) {
        return bedService.occupancyFor(departmentId, departmentName);
    }
}
