package de.pse.oys;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SmokeTest – Basistest zur Überprüfung der Erkennung der Testumgebung.
 *
 * @author uhupo
 * @version 1.0
 */
@SpringBootTest
@ActiveProfiles("test")
class SmokeTest {
    @Test
    void shouldRun() {
        System.out.println("SmokeTest running...");
        assertTrue(true);
    }

}
