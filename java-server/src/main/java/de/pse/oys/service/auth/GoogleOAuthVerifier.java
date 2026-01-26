package de.pse.oys.service.auth;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * GoogleOAuthVerifier – Der Service zur Verifizierung von Google OAuth2 Tokens.
 * Verifiziert die Gültigkeit von Google OAuth2 Tokens und extrahiert Benutzerinformationen daraus.
 * Nutzt die Google API Client Library für die Token-Verifizierung.
 *
 * @author uhupo
 * @version 1.0
 */
@Component
public class GoogleOAuthVerifier {

    private final GoogleIdTokenVerifier verifier;

    /**
     * Konstruktor mit Initialisierung des GoogleIdTokenVerifiers.
     * Der Client-ID muss entsprechend der Anwendung konfiguriert werden.
     *
     * @param clientId die Google OAuth2 Client-ID der Anwendung
     */
    public GoogleOAuthVerifier(@Value("${google.oauth2.client-id}") String clientId) {

        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), jsonFactory)
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    /**
     * Verifiziert das übergebene ID-Token und gibt die Payload zurück.
     *
     * @param idTokenString das zu verifizierende ID-Token als String
     * @return die Payload des verifizierten Tokens oder null bei Ungültigkeit
     */
    public GoogleIdToken.Payload verifyToken(String idTokenString){
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                return idToken.getPayload();
            } else {
                return  null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Getter

    /**
     * @return den GoogleIdTokenVerifier zur Token-Verifizierung
     */
    public GoogleIdTokenVerifier getVerifier() {
        return verifier;
    }
}
