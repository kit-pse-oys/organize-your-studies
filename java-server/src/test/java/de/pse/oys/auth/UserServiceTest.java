package de.pse.oys.auth;

import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.User;
import de.pse.oys.dto.auth.AuthResponseDTO;
import de.pse.oys.dto.auth.LoginDTO;
import de.pse.oys.persistence.UserRepository;
import de.pse.oys.service.UserService;
import de.pse.oys.service.auth.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private UserService userService;

    private LoginDTO validDto;

    @BeforeEach
    void setUp() {
        validDto = new LoginDTO();
        validDto.setUsername("testUser");
        validDto.setPassword("SecurePass123!");
    }

    @Test
    void testRegister_Success() {
        // Mocking für den erfolgreichen Verlauf
        when(userRepository.existsByUsername(validDto.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(validDto.getPassword())).thenReturn("encodedPassword");

        LocalUser savedUser = new LocalUser(validDto.getUsername(), "encodedPassword");
        when(userRepository.save(any(LocalUser.class))).thenReturn(savedUser);

        when(jwtProvider.createAccessToken(any())).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(any())).thenReturn("refresh-token");
        when(passwordEncoder.encode("refresh-token")).thenReturn("encoded-refresh-token");

        AuthResponseDTO response = userService.register(validDto);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(userRepository).save(any(LocalUser.class));
    }

    @Test
    void testRegister_UsernameAlreadyTaken() {
        // Fall: Benutzername existiert bereits
        when(userRepository.existsByUsername(validDto.getUsername())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.register(validDto));

        // Verifiziert die Fehlermeldung aus der UserService-Konstante ERR_USERNAME_TAKEN
        assertEquals("Der Benutzername ist bereits vergeben.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_UsernameTooShort() {
        validDto.setUsername("abc"); // Zu kurz (Min: 4)

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.register(validDto));

        assertEquals("Username zu kurz.", exception.getMessage());
    }

    @Test
    void testDeleteAccount_Success() {
        UUID userId = UUID.randomUUID();
        User user = new LocalUser(validDto.getUsername(), "hashedPassword");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteAccount(userId);

        verify(userRepository).delete(user);
    }

    @Test
    void testDeleteAccount_UserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.deleteAccount(userId));

        assertTrue(exception.getMessage().contains("nicht gefunden"));
    }
}