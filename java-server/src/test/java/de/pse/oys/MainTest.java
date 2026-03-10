package de.pse.oys;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * MainTest – Testet den Einstiegspunkt des Clients und somit ob die Anwendung korrekt startet. Dieser Test stellt sicher, dass die Hauptklasse des Clients ohne Fehler ausgeführt werden kann, was ein grundlegender Indikator für die Stabilität der Anwendung ist.
 *
 * @author uhupo
 * @version 1.0
 */
@SpringBootTest
@ActiveProfiles("integration")
public class MainTest extends BaseIntegrationTest {

    @Test
    void testMain() {
        String url = postgres.getJdbcUrl();
        String user = postgres.getUsername();
        String pass = postgres.getPassword();

        assertDoesNotThrow(() -> Main.main(new String[]{
                "--spring.datasource.url=" + url,
                "--spring.datasource.username=" + user,
                "--spring.datasource.password=" + pass,
                "--server.port=0",
                "--spring.profiles.active=integration"
        }));
    }
}

