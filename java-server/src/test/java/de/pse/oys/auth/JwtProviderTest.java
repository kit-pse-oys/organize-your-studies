package de.pse.oys.auth;


import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.User;

import de.pse.oys.persistence.UserRepository;
import de.pse.oys.service.auth.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
@SpringBootTest
@ActiveProfiles("test")
class JwtProviderTest {

   @Autowired
   private JwtProvider jwtProvider;

   @Autowired
   private UserRepository userRepository;

    @Test
    void createAccessToken_shouldReturnValidToken() {

        User user = new LocalUser("access_user", "hashed_password");
        userRepository.save(user);

        String token = jwtProvider.createAccessToken(user);

        assertNotNull(token);
        assertTrue(jwtProvider.validateToken(token));

        System.out.println("Token   :" + token);
    }

    @Test
    void createRefreshToken_shouldReturnValidToken() {
        User user = new LocalUser("refresh_user", "pw");
        userRepository.save(user);

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
        User user = new LocalUser("expired_user", "pw");
        userRepository.save(user);

        String token = shortLivedProvider.createAccessToken(user);

        Thread.sleep(5); // sicherstellen, dass Token abläuft
        assertFalse(shortLivedProvider.validateToken(token));
    }

}
