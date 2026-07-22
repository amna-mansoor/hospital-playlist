package com.hospital.controller;

import com.hospital.model.*;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final BedRepository bedRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ---- Setup / master-data endpoints (used once by the admin to configure the hospital) ----

    @PostMapping("/departments")
    public Department addDepartment(@RequestParam String name, @RequestParam(required = false) String description) {
        return departmentRepository.save(Department.builder().name(name).description(description).build());
    }

    @PostMapping("/beds")
    public Bed addBed(@RequestParam Long departmentId, @RequestParam String bedCode) {
        Department dept = departmentRepository.findById(departmentId).orElseThrow();
        return bedRepository.save(Bed.builder().department(dept).bedCode(bedCode).occupied(false).build());
    }

    @PostMapping("/doctors")
    public Doctor createDoctorProfile(@RequestParam Long userId, @RequestParam Long departmentId,
                                       @RequestParam String specialization, @RequestParam int dayOfWeek,
                                       @RequestParam String shiftStart, @RequestParam String shiftEnd,
                                       @RequestParam(defaultValue = "30") int slotLengthMinutes) {
        User user = userRepository.findById(userId).orElseThrow();
        Department dept = departmentRepository.findById(departmentId).orElseThrow();
        return doctorRepository.save(Doctor.builder()
                .user(user).department(dept).specialization(specialization)
                .dayOfWeek(dayOfWeek)
                .shiftStart(java.time.LocalTime.parse(shiftStart))
                .shiftEnd(java.time.LocalTime.parse(shiftEnd))
                .slotLengthMinutes(slotLengthMinutes)
                .build());
    }

    @PostMapping("/receptionists")
    public User createReceptionist(@RequestParam String fullName, @RequestParam String email, @RequestParam String password) {
        return userRepository.save(User.builder()
                .fullName(fullName).email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.RECEPTIONIST).enabled(true).build());
    }

    // ---- Analytics for the admin dashboard charts (Recharts on the frontend) ----

    @GetMapping("/analytics/department-load")
    public List<Map<String, Object>> departmentLoad() {
        LocalDateTime start = LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime end = LocalDateTime.now();
        List<Department> departments = departmentRepository.findAll();

        return departments.stream().map(dept -> {
            List<Doctor> docs = doctorRepository.findByDepartmentId(dept.getId());
            long total = docs.stream()
                    .mapToLong(d -> appointmentRepository.countByDoctorIdAndSlotStartBetween(d.getId(), start, end))
                    .sum();
            Map<String, Object> row = new HashMap<>();
            row.put("department", dept.getName());
            row.put("appointmentsLast30Days", total);
            return row;
        }).collect(Collectors.toList());
    }

    @GetMapping("/analytics/doctor-utilization")
    public List<Map<String, Object>> doctorUtilization() {
        LocalDateTime start = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        return doctorRepository.findAll().stream().map(doc -> {
            long booked = appointmentRepository.countByDoctorIdAndSlotStartBetween(doc.getId(), start, end);
            // rough capacity estimate: shift length / slot length * 7 days
            long shiftMinutes = java.time.Duration.between(doc.getShiftStart(), doc.getShiftEnd()).toMinutes();
            long slotsPerDay = doc.getSlotLengthMinutes() > 0 ? shiftMinutes / doc.getSlotLengthMinutes() : 0;
            long capacity = slotsPerDay * 7;
            double utilizationPct = capacity == 0 ? 0 : (booked * 100.0 / capacity);

            Map<String, Object> row = new HashMap<>();
            row.put("doctor", doc.getUser().getFullName());
            row.put("bookedLast7Days", booked);
            row.put("utilizationPct", Math.round(utilizationPct * 10.0) / 10.0);
            return row;
        }).collect(Collectors.toList());
    }

    @GetMapping("/analytics/peak-hours")
    public List<Map<String, Object>> peakHours() {
        LocalDateTime start = LocalDate.now().minusDays(30).atStartOfDay();
        List<Appointment> recent = appointmentRepository.findAll().stream()
                .filter(a -> a.getSlotStart().isAfter(start))
                .collect(Collectors.toList());

        Map<Integer, Long> byHour = recent.stream()
                .collect(Collectors.groupingBy(a -> a.getSlotStart().getHour(), Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            Map<String, Object> row = new HashMap<>();
            row.put("hour", hour + ":00");
            row.put("bookings", byHour.getOrDefault(hour, 0L));
            result.add(row);
        }
        return result;
    }

    @GetMapping("/analytics/bed-occupancy")
    public List<Map<String, Object>> bedOccupancy() {
        return departmentRepository.findAll().stream().map(dept -> {
            List<Bed> beds = bedRepository.findByDepartmentId(dept.getId());
            long occupied = beds.stream().filter(Bed::isOccupied).count();
            Map<String, Object> row = new HashMap<>();
            row.put("department", dept.getName());
            row.put("occupied", occupied);
            row.put("total", beds.size());
            return row;
        }).collect(Collectors.toList());
    }
}
