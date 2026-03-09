package de.pse.oys.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import de.pse.oys.service.auth.GoogleOAuthVerifier;
import de.pse.oys.service.auth.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
/**
 * GoogleOAuthVerifierTest – Coverage Test für die GoogleOAuthVerifier Klasse, welche die Verifizierung von Google OAuth Tokens übernimmt.
 * Es wird keine echt Verbindung zu Google APIs hergestellt, sondern die Funktionalität der verifyToken Methode wird isoliert getestet.
 *
 * @author uhupo
 * @version 1.0
 */
class GoogleOAuthVerifierTest {

    private GoogleOAuthVerifier googleOAuthVerifier;
    private GoogleIdTokenVerifier mockedInternalVerifier;

    @BeforeEach
    void setUp() throws Exception {
        // Instanz erstellen (Client-ID ist hier egal, da wir den internen Verifier gleich mocken)
        googleOAuthVerifier = new GoogleOAuthVerifier("fake-client-id");

        // internen GoogleIdTokenVerifier mocken
        mockedInternalVerifier = mock(GoogleIdTokenVerifier.class);

        Field field = GoogleOAuthVerifier.class.getDeclaredField("verifier");
        field.setAccessible(true);
        field.set(googleOAuthVerifier, mockedInternalVerifier);
    }

    @Test
    void verifyToken_success() throws Exception {
        // Arrange
        String tokenString = "valid-token";
        GoogleIdToken idToken = mock(GoogleIdToken.class);
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.set("name", "Max Mustermann");

        when(mockedInternalVerifier.verify(tokenString)).thenReturn(idToken);
        when(idToken.getPayload()).thenReturn(payload);

        // Act
        GoogleIdToken.Payload result = googleOAuthVerifier.verifyToken(tokenString);

        // Assert
        assertNotNull(result);
        assertEquals("Max Mustermann", result.get("name"));
    }

    @Test
    void verifyToken_shouldThrowInvalidTokenException_whenTokenIsNull() throws Exception {
        // Arrange
        String tokenString = "null-token";
        when(mockedInternalVerifier.verify(tokenString)).thenReturn(null);

        // Act & Assert
        InvalidTokenException ex = assertThrows(InvalidTokenException.class, () ->
                googleOAuthVerifier.verifyToken(tokenString)
        );
        assertEquals("Der übermittelte IdP-Token ist ungültig.", ex.getMessage());
    }

    @Test
    void verifyToken_shouldThrowInvalidTokenException_whenExceptionOccurs() throws Exception {
        // Arrange
        String tokenString = "error-token";
        // Simuliert einen Netzwerkfehler
        when(mockedInternalVerifier.verify(tokenString)).thenThrow(new RuntimeException("Network error"));

        // Act & Assert
        assertThrows(InvalidTokenException.class, () ->
                googleOAuthVerifier.verifyToken(tokenString)
        );
    }
}
