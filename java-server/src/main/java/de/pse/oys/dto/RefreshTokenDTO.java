package de.pse.oys.dto;

/**
 * RefreshTokenDTO – DTO für den Refresh-Token.
 *
 * @author uhupo
 * @version 1.0
 */
public class RefreshTokenDTO {
    private String refreshToken;


    /**
     * Parameterloser Konstruktor für RefreshTokenDTO.
     * Notwendig für das Mapping von JSON-Daten.
     */
    public RefreshTokenDTO() {
    }

    /**
     * Konstruktor für RefreshTokenDTO.
     *
     * @param refreshToken Der Refresh-Token als String.
     */
    public RefreshTokenDTO(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * @return Der Refresh-Token als String.
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Setzt den Refresh-Token.
     * @param refreshToken Der neue Refresh-Token als String.
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
