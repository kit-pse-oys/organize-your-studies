package de.pse.oys.dto;

/**
 * DTO für den Login-Request vom Client.
 * Unterstützt lokale Authentifizierung und externe Provider.
 */
public class LoginDTO {

    /** Benutzername für lokale Authentifizierung */
    private String username; // nur für lokale Authentifizierung relevant. Besonders dann null wenn dies der erste Anmeldeversuch ist.

    /** Passwort für lokale Authentifizierung */
    private String password; // nur für lokale Authentifizierung relevant

    /** AuthProvider: Lokal oder oidc Provider */
    private AuthProvider provider;

    /** ID-Token für externe Provider */
    private String idToken; // nur für oidc Authentifizierung relevant

    // ----- Getter & Setter -----

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public void setProvider(AuthProvider provider) {
        this.provider = provider;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
