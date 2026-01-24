package de.pse.oys.service.auth;

import de.pse.oys.domain.User;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;


/**
 * JwtProvider – Der Service zur Erstellung und Validierung von JWT-Tokens.
 * Verwendet die Bibliothek jjwt zur Handhabung von JSON Web Tokens.
 *
 * @author uhupo
 * @version 1.0
 */

@Component
public class JwtProvider {

    private static final String ERR_TOKEN_INVALID = "Token ist ungültig.";
    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    /**
     * Konstruktor mit Dependency Injection.
     * Les die Konfigurationswerte für den JWT-Provider aus den Anwendungseigenschaften fest.
     *
     * @param jwtSecret der geheime Key für die JWT-Signatur
     * @param accessTokenExpiration die Ablaufzeit des Access-Tokens in Millisekunden
     * @param refreshTokenExpiration die Ablaufzeit des Refresh-Tokens in Millisekunden
     */
    public JwtProvider(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.access.token.expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh.token.expiration}") long refreshTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }


    /**
     * Erstellt ein JWT-Access-Token für den angegebenen Benutzer.
     *
     * @param user Der Benutzer, für den das Token erstellt werden soll.
     * @return Das generierte JWT-Access-Token als String.
     */
    public String createAccessToken(User user) {
        return createToken(user, accessTokenExpiration);
    }

    /**
     * Erstellt ein JWT-Refresh-Token für den angegebenen Benutzer.
     *
     * @param user Der Benutzer, für den das Token erstellt werden soll.
     * @return Das generierte JWT-Refresh-Token als String.
     */
    public String createRefreshToken(User user) {
        return createToken(user, refreshTokenExpiration);
    }

    private String createToken(User user, long refreshTokenExpiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Validiert das JWT-Token durch Überprüfung der Signatur und des Ablaufdatums.
     *
     * @param token Das zu validierende JWT-Token.
     * @return true, wenn das Token gültig ist; false andernfalls.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Token ist ungültig: abgelaufen, manipuliert oder sonst fehlerhaft
            return false;
        }
    }

    /**
     * Extrahiert die Benutzer-ID aus dem JWT-Token.
     *
     * @param token Das JWT-Token, aus dem die Benutzer-ID extrahiert werden soll.
     * @return Die extrahierte Benutzer-ID als String.
     */
    public UUID extractUserId(String token) {
        try {
            String subject = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

            return UUID.fromString(subject);
        } catch (JwtException e) {
            throw new InvalidTokenException(ERR_TOKEN_INVALID, e);
        }
    }
}
