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
 * JwtProvider – TODO: Beschreibung ergänzen
 *
 * @author uhupo
 * @version 1.0
 */

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access.token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh.token.expiration}")
    private long refreshTokenExpiration;


    String createAccessToken(User user) {
        return createToken(user, accessTokenExpiration);
    }

    String createRefreshToken(User user) {
        return createToken(user, refreshTokenExpiration);
    }

    private String createToken(User user, long refreshTokenExpiration) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Token ist ungültig: abgelaufen, manipuliert oder sonst fehlerhaft
            return false;
        }
    }

    /**
     * Extrahiert die Benutzer-ID aus dem JWT-Token.
     * @param token Das JWT-Token, aus dem die Benutzer-ID extrahiert werden soll.
     * @return Die extrahierte Benutzer-ID als String.
     */
    UUID getUserIdFromToken(String token) {
        try {
            String subject = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

            return UUID.fromString(subject);
        } catch (JwtException e) {
            throw new InvalidTokenException("Token ungültig", e);
        }
    }
}
