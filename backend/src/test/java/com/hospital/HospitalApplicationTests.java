package com.hospital;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: confirms the whole Spring application context boots correctly
 * (all beans wire up, JPA entities are valid, security config loads, etc).
 * This is the test GitHub Actions runs on every push - see .github/workflows/backend-ci.yml
 */
@SpringBootTest
class HospitalApplicationTests {

    @Test
    void contextLoads() {
        // If this test passes, your whole backend configuration is sound.
    }
}
