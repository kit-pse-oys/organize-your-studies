package de.pse.oys.auth;

import de.pse.oys.BaseIntegrationTest;
import de.pse.oys.domain.enums.UserType;
import de.pse.oys.dto.RefreshTokenDTO;
import de.pse.oys.dto.auth.LoginDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static de.pse.oys.dto.auth.AuthType.BASIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    private static final String AUTH_BASE = "/api/v1/users";
    private static final String LOGIN = AUTH_BASE + "/login";
    private static final String REFRESH = AUTH_BASE + "/refresh";

    private final String rawPassword = "TestPass123!";
    private String authHeader;

    @BeforeEach
    void setUp() throws Exception {
        authHeader = getAuthHeader("integrationTestUser", rawPassword);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void testLocalLoginRequest() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setAuthType(BASIC);
        loginDTO.setUsername("integrationTestUser");
        loginDTO.setPassword(rawPassword);

        mockMvc.perform(post(LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void testLocalLoginMappingMissingExternalFields() throws Exception {
        String jsonMissingFields = """
        {
            "username": "integrationTestUser",
            "password": "TestPass123!",
            "authType": "BASIC"
        }
        """;

        LoginDTO mappedDto = objectMapper.readValue(jsonMissingFields, LoginDTO.class);

        assertEquals("integrationTestUser", mappedDto.getUsername());
        assertEquals("TestPass123!", mappedDto.getPassword());
        assertEquals(BASIC, mappedDto.getAuthType());
        assertNull(mappedDto.getExternalToken());
        assertNull(mappedDto.getProvider());
    }

    @Test
    void testExternalLoginMappingMissingFields() throws Exception {
        String jsonExternal = """
        {
            "authType": "OIDC",
            "externalToken": "xxx",
            "provider": "GOOGLE"
        }
        """;

        LoginDTO mappedDto = objectMapper.readValue(jsonExternal, LoginDTO.class);

        assertEquals(de.pse.oys.dto.auth.AuthType.OIDC, mappedDto.getAuthType());
        assertEquals("xxx", mappedDto.getExternalToken());
        assertEquals(UserType.GOOGLE, mappedDto.getProvider());
        assertNull(mappedDto.getUsername());
        assertNull(mappedDto.getPassword());
    }

    @Test
    void testRefreshRequest() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setAuthType(BASIC);
        loginDTO.setUsername("integrationTestUser");
        loginDTO.setPassword(rawPassword);

        String loginResponse = mockMvc.perform(post(LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();

        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO(refreshToken);

        mockMvc.perform(post(REFRESH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void testRegisterSuccess() throws Exception {
        LoginDTO registrationDTO = new LoginDTO();
        registrationDTO.setUsername("newUser");
        registrationDTO.setPassword("StrongPass123!");
        registrationDTO.setAuthType(BASIC);

        mockMvc.perform(post(AUTH_BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void testRegisterFailShortUsername() throws Exception {
        LoginDTO shortUser = new LoginDTO();
        shortUser.setUsername("ab");
        shortUser.setPassword("ValidPass123!");

        mockMvc.perform(post(AUTH_BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterFailsForInvalidInputs() throws Exception {
        // Verschiedene ungültige Kombinationen von Benutzernamen und Passwörtern
        String[][] testData = {
                {"validUser", "short"},             // Passwort zu kurz
                {"integrationTestUser", "Valid1!"}, // Username vergeben
                {"", "ValidPass123!"}               // Username leer
        };

        for (String[] data : testData) {
            String username = data[0];
            String password = data[1];

            LoginDTO dto = new LoginDTO();
            dto.setUsername(username);
            dto.setPassword(password);
            dto.setAuthType(BASIC);

            mockMvc.perform(post(AUTH_BASE + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void testDeleteAccountSuccess() throws Exception {
        UUID userId = userRepository.findByUsernameAndUserType("integrationTestUser", UserType.LOCAL).get().getId();

        mockMvc.perform(delete(AUTH_BASE)
                        .header("Authorization", authHeader))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertFalse(userRepository.existsById(userId));
    }

    @Test
    void testDeleteAccountNotFound() throws Exception {
        mockMvc.perform(delete(AUTH_BASE)
                        .header("Authorization", UUID.randomUUID().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void testRegisterFailPasswordNull() throws Exception {
        // Explizit fehlende Passwort-Feld testen
        String jsonWithNullPassword = """
    {
        "username": "validUser",
        "password": null,
        "authType": "BASIC"
    }
    """;

        mockMvc.perform(post(AUTH_BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithNullPassword))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterFailUsernameNull() throws Exception {
        String jsonWithNullUsername = """
    {
        "username": null,
        "password": "ValidPass123!",
        "authType": "BASIC"
    }
    """;

        mockMvc.perform(post(AUTH_BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithNullUsername))
                .andExpect(status().isBadRequest());
    }
}