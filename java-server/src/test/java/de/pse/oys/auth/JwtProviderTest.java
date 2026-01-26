package de.pse.oys.auth;


import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.User;

import de.pse.oys.service.auth.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * JwtProviderTest – Test für die JWT Token Erstellung und Validierung.
 * Ausgelagerter Teil des Tests für den AuthService und UserService (Registrieren von lokalen Benutzern).
 *
 * @author uhupo
 * @version 1.0
 */
@SpringBootTest(classes = JwtProvider.class)
@ActiveProfiles("test")
class JwtProviderTest {

   @Autowired
   private JwtProvider jwtProvider;

    @Test
    void createAccessToken_shouldReturnValidToken() {

        User user = new LocalUser(UUID.randomUUID(), "access_user", "hashed_password", "salt");

        String token = jwtProvider.createAccessToken(user);

        assertNotNull(token);
        assertTrue(jwtProvider.validateToken(token));

        System.out.println("Token   :" + token);
    }

    @Test
    void createRefreshToken_shouldReturnValidToken() {
        User user = new LocalUser(UUID.randomUUID(), "refresh_user", "pw", "salt");

        String refreshToken = jwtProvider.createRefreshToken(user);
        assertNotNull(refreshToken);
        assertTrue(jwtProvider.validateToken(refreshToken));
    }

    @Test
    void invalidToken_shouldFailValidation() {
        String fakeToken = "this.is.not.a.jwt";

        assertFalse(jwtProvider.validateToken(fakeToken));
    }

    @Test
    void expiredToken_shouldFailValidation() throws InterruptedException {
        // Eigener Provider nur für diesen Test
        JwtProvider shortLivedProvider = new JwtProvider(
                "myDefaultSecretHuber1234567890TestJWTSecretForDevPurposesOnly!!!", //512 Bit erwartet
                1, // accessTokenExpirationMs
                1000 // refreshTokenExpirationMs
        );
        User user = new LocalUser(UUID.randomUUID(), "expired_user", "pw", "salt");
        String token = shortLivedProvider.createAccessToken(user);

        Thread.sleep(5); // sicherstellen, dass Token abläuft
        assertFalse(shortLivedProvider.validateToken(token));
    }

}
