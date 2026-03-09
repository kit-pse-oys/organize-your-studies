package de.pse.oys.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import de.pse.oys.domain.ExternalUser;
import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.enums.UserType;
import de.pse.oys.dto.RefreshTokenDTO;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

/**
 * AuthServiceTest – Unit-Tests für den AuthService.
 * Testet die Authentifizierungslogik für lokale und OAuth2 Benutzer.
 *
 * @author uhupo
 * @version 1.0
 */
@SpringBootTest
@ActiveProfiles("test")
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
        String hashedPassword = "hashedpass";

        LocalUser user = mock(LocalUser.class);
        when(user.getHashedPassword()).thenReturn(hashedPassword);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(userRepository.findByUsernameAndUserType(username, UserType.LOCAL))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);
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
        verify(user).setRefreshTokenHash(any());
        verify(userRepository).save(user);
    }

    @Test
    void testLoginGoogleUserSuccess_NewUser() {
        // Arrange
        String googleSub = "google-123";
        String name = "Test User";
        String token = "dummy-token";

        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getSubject()).thenReturn(googleSub);
        when(payload.get("name")).thenReturn(name);

        when(googleOAuthVerifier.verifyToken(token)).thenReturn(payload);
        when(userRepository.findByExternalSubjectIdAndUserType(googleSub, UserType.GOOGLE))
                .thenReturn(Optional.empty());

        // Captor für neuen User
        ArgumentCaptor<ExternalUser> userCaptor = ArgumentCaptor.forClass(ExternalUser.class);
        when(jwtProvider.createAccessToken(any())).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(any())).thenReturn("refresh-token");

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setAuthType(AuthType.OIDC);
        loginDTO.setProvider(UserType.GOOGLE);
        loginDTO.setExternalToken(token);

        // Act
        AuthResponseDTO response = authService.login(loginDTO);

        // Assert
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        // Prüfen, dass ein neuer User gespeichert wurde
        verify(userRepository).save(userCaptor.capture());
        ExternalUser savedUser = userCaptor.getValue();
        assertEquals(googleSub, savedUser.getExternalSubjectId());
        assertEquals(UserType.GOOGLE, savedUser.getUserType());
    }

    @Test
    void refreshToken_withInvalidToken_shouldThrowException() {
        RefreshTokenDTO refreshDTO = new RefreshTokenDTO("invalid-token");

        when(jwtProvider.validateToken("invalid-token")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authService.refreshToken(refreshDTO)
        );

        assertEquals("Ungültiges Refresh-Token.", exception.getMessage());
    }

    @Test
    void refreshToken_withValidToken_shouldReturnNewAccessToken() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String refreshToken = "valid-refresh-token";
        String newAccessToken = "new-access-token";
        String username = "testuser";
        String refreshTokenHash = "hashed-refresh-token";

        // User-Mock
        LocalUser user = mock(LocalUser.class);
        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn(username);
        when(user.getRefreshTokenHash()).thenReturn(refreshTokenHash);

        // Mocks für Token-Validierung und Extraktion
        when(jwtProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtProvider.extractUserId(refreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(refreshToken, refreshTokenHash)).thenReturn(true);
        when(jwtProvider.createAccessToken(user)).thenReturn(newAccessToken);

        RefreshTokenDTO refreshDTO = new RefreshTokenDTO(refreshToken);

        // Act
        AuthResponseDTO response = authService.refreshToken(refreshDTO);

        // Assert
        assertEquals(newAccessToken, response.getAccessToken());
    }

    @Test
    void refreshToken_withMismatchedHash_shouldThrowException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String refreshToken = "valid-token-format-but-wrong-session";
        String wrongHashInDb = "some-other-hash";

        LocalUser user = mock(LocalUser.class);
        when(user.getRefreshTokenHash()).thenReturn(wrongHashInDb);

        when(jwtProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtProvider.extractUserId(refreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(refreshToken, wrongHashInDb)).thenReturn(false);

        RefreshTokenDTO refreshDTO = new RefreshTokenDTO(refreshToken);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                authService.refreshToken(refreshDTO)
        );
        verify(jwtProvider, never()).createAccessToken(any());
    }

    @Test
    void login_withInvalidExternalToken_shouldThrowException() {
        String token = "invalid-google-token";
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setAuthType(AuthType.OIDC);
        loginDTO.setProvider(UserType.GOOGLE);
        loginDTO.setExternalToken(token);

        // Mock liefert null -> Trigger für die Exception
        when(googleOAuthVerifier.verifyToken(token)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                authService.login(loginDTO)
        );
        assertEquals("Ungültiges externes Token übermittelt.", ex.getMessage());
    }

    @Test
    void login_googleUserWithoutName_shouldUseDefaultName() {
        String token = "token-without-name";
        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getSubject()).thenReturn("12345");
        when(payload.get("name")).thenReturn(null);

        when(googleOAuthVerifier.verifyToken(token)).thenReturn(payload);
        when(userRepository.findByExternalSubjectIdAndUserType(anyString(), any()))
                .thenReturn(Optional.empty());

        // Captor um den gespeicherten User zu prüfen
        ArgumentCaptor<ExternalUser> userCaptor = ArgumentCaptor.forClass(ExternalUser.class);

        LoginDTO dto = new LoginDTO();
        dto.setAuthType(AuthType.OIDC);
        dto.setProvider(UserType.GOOGLE);
        dto.setExternalToken(token);

        authService.login(dto);

        verify(userRepository).save(userCaptor.capture());
        assertEquals("Google User", userCaptor.getValue().getUsername());
    }

    @Test
    void login_localUserNotFound_shouldThrowIllegalStateException() {
        // Arrange
        String username = "nonexistent";
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setAuthType(AuthType.BASIC);
        loginDTO.setUsername(username);
        loginDTO.setPassword("password");

        when(userRepository.findByUsernameAndUserType(username, UserType.LOCAL))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                authService.login(loginDTO)
        );

        assertTrue(exception.getMessage().contains("Der Benutzername existiert, findet aber keinen zugehörigen lokalen Benutzer"));
    }

    @Test
    void login_wrongPassword_shouldThrowIllegalArgumentException() {
        // Arrange
        String username = "localuser";
        String wrongPassword = "wrong-password";
        String correctHash = "correct-hash";

        LocalUser user = mock(LocalUser.class);
        when(user.getHashedPassword()).thenReturn(correctHash);

        when(userRepository.findByUsernameAndUserType(username, UserType.LOCAL))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(wrongPassword, correctHash)).thenReturn(false);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setAuthType(AuthType.BASIC);
        loginDTO.setUsername(username);
        loginDTO.setPassword(wrongPassword);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authService.login(loginDTO)
        );

        assertEquals("Ungültige Anmeldeinformationen.", exception.getMessage());
    }

    @Test
    void refreshToken_withNullStoredHash_shouldThrowException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String refreshToken = "valid-token";
        LocalUser user = mock(LocalUser.class);

        when(jwtProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtProvider.extractUserId(refreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        when(user.getRefreshTokenHash()).thenReturn(null);

        RefreshTokenDTO refreshDTO = new RefreshTokenDTO(refreshToken);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authService.refreshToken(refreshDTO)
        );

        assertEquals("Refresh-Token stimmt nicht überein.", exception.getMessage());
        // Sicherstellen, dass der PasswordEncoder gar nicht erst gefragt wurde,
        // da die Bedingung schon vorher true war (Short-Circuit)
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}
