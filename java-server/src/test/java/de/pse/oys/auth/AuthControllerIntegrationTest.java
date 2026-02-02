package de.pse.oys.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.enums.UserType;
import de.pse.oys.dto.RefreshTokenDTO;
import de.pse.oys.dto.auth.LoginDTO;
import de.pse.oys.persistence.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthControllerIntegrationTest – Integrationstest für den AuthController und AuthService.
 * Verwendet hierbei den echten Datenbankzugriff um den kompletten Authentifizierungsprozess zu testen.
 *
 * @author uhupo
 * @version 1.0
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("integration")
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    // Endpunkt-Pfade, die vom AuthController verwendet werden
    private static final String AUTH_BASE = "/auth";
    private static final String LOGIN = AUTH_BASE + "/login";
    private static final String REFRESH = AUTH_BASE + "/refresh";

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("oys")
                    .withUsername("oys")
                    .withPassword("oys");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private LocalUser testUser;
    private final String rawPassword = "TestPass123!";

    @BeforeEach
    void setUp() {
        // Testuser anlegen
        String hashedPassword = passwordEncoder.encode(rawPassword);
        testUser = new LocalUser("integrationTestUser", hashedPassword);
        userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        // Testdaten aus der DB entfernen (sonst Sideeffekte bei weiteren Tests)
        userRepository.deleteAll();
    }

    @Test
    void testLocalLoginRequest() throws Exception {
        // LOGIN Request mit gültigen Anmeldedaten
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setType(de.pse.oys.dto.auth.AuthType.BASIC);
        loginDTO.setUsername(testUser.getUsername());
        loginDTO.setPassword(rawPassword);

        String loginJson = objectMapper.writeValueAsString(loginDTO);
        System.out.println("Login JSON: " + loginJson); // Debug-Ausgabe

        // --- Login + Assertions ---
        MvcResult result = mockMvc.perform(post(LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        System.out.println("Login Response: " + result.getResponse().getContentAsString());
    }

    @Test
    void testLocalLoginMappingMissingExternalFields() throws Exception {
        // Test-JSON mit allen erforderlichen Feldern für ein lokales Login, der Client erspart sich es null Felder zu senden
        // JSON ohne optionales Feld "externalToken" und "authProvider"
        String jsonMissingFields = """
        {
            "username": "integrationTestUser",
            "password": "TestPass123!",
            "authType": "BASIC"
        }
        """;

        LoginDTO mappedDto = objectMapper.readValue(jsonMissingFields, LoginDTO.class);

        // Assertions
        assertEquals("integrationTestUser", mappedDto.getUsername());
        assertEquals("TestPass123!", mappedDto.getPassword());
        assertEquals(de.pse.oys.dto.auth.AuthType.BASIC, mappedDto.getType());

        // Felder, die nicht im JSON sind, sollten null sein
        assertNull(mappedDto.getExternalToken());
        assertNull(mappedDto.getProvider());
    }

    @Test
    void testExternalLoginMappingMissingFields() throws Exception {
        // JSON nur mit den Pflichtfeldern für ein externes Login bzw. Just-in-time Provisioning.
        // Auch hier spart sich der Client null Felder zu senden
        String jsonExternal = """
        {
            "authType": "OIDC",
            "externalToken": "xxx",
            "provider": "GOOGLE"
        }
        """;

        LoginDTO mappedDto = objectMapper.readValue(jsonExternal, LoginDTO.class);

        // Assertions
        assertEquals(de.pse.oys.dto.auth.AuthType.OIDC, mappedDto.getType());
        assertEquals("xxx", mappedDto.getExternalToken());
        assertEquals(UserType.GOOGLE, mappedDto.getProvider());


        assertNull(mappedDto.getUsername());
        assertNull(mappedDto.getPassword());
    }



    @Test
    void testRefreshRequest() throws Exception {
        // LOGIN als Setup für REFRESH (Login wird im eigenen Test bereits getestet)
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setType(de.pse.oys.dto.auth.AuthType.BASIC);
        loginDTO.setUsername(testUser.getUsername());
        loginDTO.setPassword(rawPassword);

        String loginJson = objectMapper.writeValueAsString(loginDTO);
        String loginResponse = mockMvc.perform(post(LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();

        // REFRESH SETUP
        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO(refreshToken);
        String refreshJson = objectMapper.writeValueAsString(refreshTokenDTO);
        System.out.println("Refresh JSON: " + refreshJson);

        // --- Refresh + Assertions ---
        mockMvc.perform(post(REFRESH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").value(refreshToken));
    }
}
