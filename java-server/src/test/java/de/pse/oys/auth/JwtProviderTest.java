package de.pse.oys.auth;


import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.User;

import de.pse.oys.service.auth.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * JwtProviderTest – TODO: Beschreibung ergänzen
 *
 * @author uhupo
 * @version 1.0
 */
@SpringBootTest
public class JwtProviderTest {

   @Autowired
   private JwtProvider jwtProvider;

    @Test
    void createAccessToken_shouldReturnValidToken() {

        User user = new LocalUser(UUID.randomUUID(), "test_username", "hashed_password", "test_salt");

        String token = jwtProvider.createAccessToken(user);

        assertNotNull(token);
        assertTrue(jwtProvider.validateToken(token));

        System.out.println("Token   :" + token);
    }
}
