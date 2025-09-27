package io.github.bsayli.customerservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Tag("integration")
@DisplayName("Integration Test: Application Context")
class CustomerServiceApplicationIT {

    @Test
    @DisplayName("Spring context should load without issues")
    void contextLoads() {
        // If the context fails to start, this test will fail
    }
}