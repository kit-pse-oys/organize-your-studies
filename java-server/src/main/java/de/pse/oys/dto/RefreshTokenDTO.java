package de.pse.oys.dto;

/**
 * RefreshTokenDTO – DTO für den Refresh-Token.
 *
 * @author uhupo
 * @version 1.0
 */
public class RefreshTokenDTO {
    private final String refreshToken;

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
}
