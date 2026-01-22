package de.pse.oys.dto;

import de.pse.oys.domain.enums.UserType;

/**
 * DTO für den Login-Request vom Client.
 * Unterstützt lokale Authentifizierung und externe Provider.
 */
public class LoginDTO {

    /** Benutzername für lokale Authentifizierung */
    private String username; // null, wenn externer Account. Besonders dann null wenn dies der erste Anmeldeversuch ist.

    /** Passwort für lokale Authentifizierung */
    private String password; // null, wenn externer Account

    /** UserType: LOCAL oder externer Provider */
    private UserType provider;

    /** ID-Token für externe Provider */
    private String idToken; // null, wenn lokaler Account

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

    public UserType getProvider() {
        return provider;
    }

    public void setProvider(UserType provider) {
        this.provider = provider;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
