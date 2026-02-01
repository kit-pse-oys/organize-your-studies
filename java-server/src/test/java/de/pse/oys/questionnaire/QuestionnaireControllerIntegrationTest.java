package de.pse.oys.questionnaire;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.User;
import de.pse.oys.domain.enums.TimeSlot;
import de.pse.oys.dto.QuestionnaireDTO;
import de.pse.oys.dto.auth.AuthType;
import de.pse.oys.dto.auth.LoginDTO;
import de.pse.oys.persistence.UserRepository;
import jakarta.transaction.Transactional;
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

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * QuestionnaireControllerIntegrationTest – Integrationstest für den QuestionnaireController und QuestionnaireService.
 * Verwendet hierbei den echten Datenbankzugriff um den kompletten Fragebogen-Prozess zu testen.
 *
 * @author uhupo
 * @version 1.0
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("integration")
@AutoConfigureMockMvc
class QuestionnaireControllerIntegrationTest {

    private static final String QUESTIONNAIRE_BASE = "/questionnaire";
    private static final String SUBMIT = QUESTIONNAIRE_BASE + "/submit";
    private static final String STATUS = QUESTIONNAIRE_BASE + "/status";

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("oys")
                    .withUsername("oys")
                    .withPassword("oys");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
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
        testUser = new LocalUser("nilsiberUser", hashedPassword);
        userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    private String loginAndGetToken() throws Exception {
        // Die Method wird verwendet um einen Access Token für den Testuser zu erhalten.
        // Dadurch kann der Test unter den realen Sicherheitsbedingungen durch UserPrincipal durchgeführt werden.
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setAuthType(AuthType.BASIC);
        loginDTO.setUsername(testUser.getUsername());
        loginDTO.setPassword(rawPassword);

        String loginJson = objectMapper.writeValueAsString(loginDTO);

        // Login Request durchführen
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        // Access Token aus der Response extrahieren
        String response = loginResult.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }


    @Test
    @Transactional
    void testSubmitQuestionnaireAndStatus() throws Exception {
        // Login und Token holen und Nutzer in den SecurityContext setzen (da SecurityContext in MockMvc nicht automatisch gesetzt wird)
        String accessToken = loginAndGetToken();

        // Questionnaire DTO vorbereiten mit ***GÜLTIGEN*** Daten
        // Beispeieldaten: minUnitDuration=30, maxUnitDuration=90, maxDayLoad=6, preferredPauseDuration=10,
        // timeBeforeDeadlines=2, preferredStudyDays=[MONDAY, WEDNESDAY], preferredStudyTimes=[MORNING, EVENING]
        QuestionnaireDTO dto = new QuestionnaireDTO();
        dto.setMinUnitDuration(30);
        dto.setMaxUnitDuration(90);
        dto.setMaxDayLoad(6);
        dto.setPreferredPauseDuration(10);
        dto.setTimeBeforeDeadlines(2);
        dto.setPreferredStudyDays(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        dto.setPreferredStudyTimes(Set.of(TimeSlot.MORNING, TimeSlot.EVENING));

        String dtoJson = objectMapper.writeValueAsString(dto);

        // Fragebogen absenden mit echtem JWT und speichern lassen
        mockMvc.perform(post(SUBMIT)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))
                .andExpect(status().isOk());

        // Status abfragen (Wurde der Fragebogen bereits ausgefüllt?)
        MvcResult statusResult = mockMvc.perform(get(STATUS)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        String statusContent = statusResult.getResponse().getContentAsString();
        assertTrue(Boolean.parseBoolean(statusContent), "Questionnaire sollte als ausgefüllt markiert sein.");

        // DB prüfen
        User savedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertNotNull(savedUser.getPreferences());
        assertTrue(savedUser.getPreferences().getPreferredDays().contains(DayOfWeek.MONDAY));
        assertTrue(savedUser.getPreferences().getPreferredTimeSlots().contains(TimeSlot.MORNING));
    }
}
