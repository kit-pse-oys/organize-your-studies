package de.pse.oys.dto.auth;

import java.util.UUID;

/**
 * Datentransferobjekt für die Antwort nach einer erfolgreichen Authentifizierung.
 * Enthält die Sitzungstoken sowie die Identifikationsdaten des angemeldeten Nutzers.
 * @author utgid
 * @version 1.0
 */
public class AuthResponseDTO {

    /** Der Access-Token für die Autorisierung der API-Anfragen. */
    private String accessToken;

    /** Der Refresh-Token zum Erneuern des Access-Tokens. */
    private String refreshToken;

    /** Die eindeutige Kennung des authentifizierten Nutzers. */
    private UUID userId;

    /** Der Anzeigename des Nutzers. */
    private String username;

    /**
     * Standardkonstruktor für die Deserialisierung.
     */
    public AuthResponseDTO() {
    }

    // Getter

    /** @return Der aktuelle Access-Token. */
    public String getAccessToken() { return accessToken; }

    /** @return Der aktuelle Refresh-Token. */
    public String getRefreshToken() { return refreshToken; }

    /** @return Die ID des Nutzers. */
    public UUID getUserId() { return userId; }

    /** @return Der Nutzername. */
    public String getUsername() { return username; }

    // Setter

    /** @param accessToken Der neue Access-Token. */
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    /** @param refreshToken Der neue Refresh-Token. */
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    /** @param userId Die ID des authentifizierten Nutzers. */
    public void setUserId(UUID userId) { this.userId = userId; }

    /** @param username Der Anzeigename des Nutzers. */
    public void setUsername(String username) { this.username = username; }
}