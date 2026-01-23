package de.pse.oys.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import de.pse.oys.domain.ExternalUser;
import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.enums.UserType;
import de.pse.oys.dto.RefreshTokenDTO;
import de.pse.oys.dto.UserDTO;
import de.pse.oys.dto.auth.AuthResponseDTO;
import de.pse.oys.dto.auth.AuthType;
import de.pse.oys.dto.auth.LoginDTO;
import de.pse.oys.persistence.UserRepository;

import de.pse.oys.service.auth.AuthService;
import de.pse.oys.service.auth.GoogleOAuthVerifier;
import de.pse.oys.service.auth.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

/**
 * AuthServiceTest – Unit-Tests für den AuthService.
 * Testet die Authentifizierungslogik für lokale und OAuth2 Benutzer.
 *
 * @author uhupo
 * @version 1.0
 */
class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtProvider jwtProvider;
    private GoogleOAuthVerifier googleOAuthVerifier;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtProvider = mock(JwtProvider.class);
        googleOAuthVerifier = mock(GoogleOAuthVerifier.class);

        authService = new AuthService(userRepository, passwordEncoder, jwtProvider, googleOAuthVerifier);
    }

    @Test
    void testLoginLocalUserSuccess() {
        // Arrange
        String username = "localuser";
        String password = "pass123";
        String salt = "salt";
        String hashedPassword = "hashedpass";

        LocalUser user = mock(LocalUser.class);
        when(user.getSalt()).thenReturn(salt);
        when(user.getHashedPassword()).thenReturn(hashedPassword);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(userRepository.findByNameAndType(username, UserType.LOCAL))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password + salt, hashedPassword)).thenReturn(true);
        when(jwtProvider.createAccessToken(user)).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(user)).thenReturn("refresh-token");

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setAuthType(AuthType.BASIC);
        loginDTO.setUsername(username);
        loginDTO.setPassword(password);

        // Act
        AuthResponseDTO response = authService.login(loginDTO);

        // Assert
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(username, response.getUsername());
        verify(user).setRefreshTokenHash(any());
        verify(userRepository).save(user);
    }

    @Test
    void testLoginGoogleUserSuccess_NewUser() throws Exception {
        // Arrange
        String googleSub = "google-123";
        String name = "Test User";
        String token = "dummy-token";

        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getSubject()).thenReturn(googleSub);
        when(payload.get("name")).thenReturn(name);

        when(googleOAuthVerifier.verifyToken(token)).thenReturn(payload);
        when(userRepository.findByExternalSubjectIdAndType(googleSub, UserType.GOOGLE))
                .thenReturn(Optional.empty());

        // Captor für neuen User
        ArgumentCaptor<ExternalUser> userCaptor = ArgumentCaptor.forClass(ExternalUser.class);
        when(jwtProvider.createAccessToken(any())).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(any())).thenReturn("refresh-token");

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setAuthType(AuthType.OIDC);
        loginDTO.setAuthProvider(UserType.GOOGLE);
        loginDTO.setExternalToken(token);

        // Act
        AuthResponseDTO response = authService.login(loginDTO);

        // Assert
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(name, response.getUsername());

        // Prüfen, dass ein neuer User gespeichert wurde
        verify(userRepository).save(userCaptor.capture());
        ExternalUser savedUser = userCaptor.getValue();
        assertEquals(googleSub, savedUser.getExternalSubjectId());
        assertEquals(UserType.GOOGLE, savedUser.getUserType());
    }

    @Test
    void refreshToken_withInvalidToken_shouldThrowException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId.toString());

        RefreshTokenDTO refreshDTO = new RefreshTokenDTO("invalid-token");

        when(jwtProvider.validateToken("invalid-token")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authService.refreshToken(userDTO, refreshDTO)
        );

        assertEquals("Ungültiges Refresh-Token.", exception.getMessage());
    }

}
