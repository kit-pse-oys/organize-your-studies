package de.pse.oys;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * BaseIntegrationTest – Abstrakte Basisklasse für Integrationstests mit Testcontainers und Spring Boot.
 * Optimiert die Testausführung durch Wiederverwendung eines PostgreSQL-Containers.
 *
 * @author uhupo
 * @version 1.0
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("integration")
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withReuse(true); // Erlaubt Wiederverwendung des Containers

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.hikari.connection-timeout", () -> "1000");
    }
}
