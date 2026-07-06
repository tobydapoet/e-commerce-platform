package com.example.e_commerce;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("E-Commerce application tests")
class ECommerceApplicationTests {

    @Nested
    @DisplayName("Application Context")
    class ApplicationContextTests {

        @DisplayName("Context should load")
        @Test
        void contextLoads() {
        }
    }

}
