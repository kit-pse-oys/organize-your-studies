package de.pse.oys.dto.auth;

import de.pse.oys.domain.enums.UserType;

/**
 * DTO für den Login-Request vom Client.
 * Unterstützt lokale Authentifizierung und externe Provider.
 * Da für externe Accounts das Login und die Registrierung äquivalent sind,
 * wird dieses DTO sowohl für Login als auch Registrierung dieser Accounts genutzt.
 */
public class LoginDTO {

    /** Benutzername für lokale Authentifizierung */
    private String username; // nur für lokale Authentifizierung relevant. Besonders dann null wenn dies der erste Anmeldeversuch ist.

    /** Passwort für lokale Authentifizierung */
    private String password; // nur für lokale Authentifizierung relevant

    /** AuthType: Unterscheidung zwischen lokal und extern */
    private AuthType type; // gibt an ob lokale oder externe Authentifizierung genutzt wird

    /** Externer Authentifizierungsprovider */
    private UserType provider; // nur für externe Authentifizierung relevant

    /** ID-Token für externe Provider */
    private String externalToken; // nur für oidc Authentifizierung relevant

    // ----- Getter & Setter -----

    /**
     * @return den Username als String
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username der Username als String
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return das password als String
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password das password als String
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return den AuthType
     */
    public AuthType getAuthType() {
        return type;
    }

    /**
     * @param type den AuthType
     */
    public void setAuthType(AuthType type) {
        this.type = type;
    }

    /**
     * @return den Authentifizierungsprovider
     */
    public UserType getProvider() {
        return provider;
    }

    /**
     * @param provider den Authentifizierungsprovider
     */
    public void setProvider(UserType provider) {
        this.provider = provider;
    }

    /**
     * @return den Token des externen Authentifizierungsproviders
     */
    public String getExternalToken() {
        return externalToken;
    }

    /**
     * @param externalToken den zu setzenden Token String
     */
    public void setExternalToken(String externalToken) {
        this.externalToken = externalToken;
    }
}
