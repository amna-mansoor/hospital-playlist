package com.hospital.controller;

import com.hospital.model.Waitlist;
import com.hospital.repository.UserRepository;
import com.hospital.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/waitlist")
@RequiredArgsConstructor
public class WaitlistController {

    private final WaitlistService waitlistService;
    private final UserRepository userRepository;

    @PostMapping("/join")
    public Waitlist join(@RequestParam Long doctorId, @RequestParam String date, Authentication auth) {
        Long userId = userRepository.findByEmail(auth.getName()).orElseThrow().getId();
        return waitlistService.joinWaitlist(userId, doctorId, LocalDate.parse(date));
    }
}
