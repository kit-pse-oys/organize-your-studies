package de.pse.oys.security;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * UserPrincipalTest – Unit-Test für die UserPrincipal Klasse.
 * Überprüft die korrekte Initialisierung der Felder und die Implementierung der Methoden, insbesondere die von Spring Security geforderten Overrides.
 *
 * @author uhupo
 * @version 1.0
 */
class UserPrincipalTest {

    @Test
    void testUserPrincipalMethods() {
        // GIVEN
        UUID userId = UUID.randomUUID();
        String username = "testUser";
        String password = "securePassword";

        // WHEN
        UserPrincipal principal = new UserPrincipal(userId, username, password);

        // THEN
        assertEquals(userId, principal.getUserId());
        assertEquals(username, principal.getUsername());
        assertEquals(password, principal.getPassword());

        // Testet die Spring Security Overrides (für die Coverage)
        assertNull(principal.getAuthorities(), "Authorities sollten null zurückgeben");
        assertTrue(principal.isAccountNonExpired(), "Account sollte nicht abgelaufen sein");
        assertTrue(principal.isAccountNonLocked(), "Account sollte nicht gesperrt sein");
        assertTrue(principal.isCredentialsNonExpired(), "Credentials sollten nicht abgelaufen sein");
        assertTrue(principal.isEnabled(), "User sollte aktiviert sein");
    }
}
