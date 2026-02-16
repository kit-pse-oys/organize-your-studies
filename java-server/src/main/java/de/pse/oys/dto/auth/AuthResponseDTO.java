package de.pse.oys.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Datentransferobjekt für die Antwort nach einer erfolgreichen Authentifizierung.
 * Enthält die Sitzungstoken sowie die Identifikationsdaten des angemeldeten Nutzers.
 * @author utgid
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO {

    /** Der Access-Token für die Autorisierung der API-Anfragen. */
    private String accessToken;

    /** Der Refresh-Token zum Erneuern des Access-Tokens. */
    private String refreshToken;

    /**
     * Standardkonstruktor für die Deserialisierung.
     */
    public AuthResponseDTO() {
    }

    /**
     * Konstruktor mit allen Attributen.
     * Wird aufgerufen, um eine vollständige Authentifizierungsantwort zu erstellen.
     *
     * @param accessToken der ausgestellte Access-Token
     * @param refreshToken der ausgestellte Refresh-Token
     */
    public AuthResponseDTO(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    /**
     * Konstruktor für Fälle, in denen nur ein Access-Token zurückgegeben wird (z.B. bei Token-Refresh).
     *
     * @param accessToken der ausgestellte Access-Token
     */
    public AuthResponseDTO(String accessToken) {
        this.accessToken = accessToken;
    }

    // Getter

    /** @return Der aktuelle Access-Token. */
    public String getAccessToken() { return accessToken; }

    /** @return Der aktuelle Refresh-Token. */
    public String getRefreshToken() { return refreshToken; }

    // Setter

    /** @param accessToken Der neue Access-Token. */
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    /** @param refreshToken Der neue Refresh-Token. */
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

}