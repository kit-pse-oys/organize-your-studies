package de.pse.oys.rating;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.pse.oys.BaseIntegrationTest;
import de.pse.oys.domain.CostMatrix;
import de.pse.oys.domain.ExamTask;
import de.pse.oys.domain.LearningUnit;
import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.Module;
import de.pse.oys.domain.Task;
import de.pse.oys.domain.enums.AchievementLevel;
import de.pse.oys.domain.enums.ConcentrationLevel;
import de.pse.oys.domain.enums.ModulePriority;
import de.pse.oys.domain.enums.PerceivedDuration;
import de.pse.oys.domain.enums.UnitStatus;
import de.pse.oys.dto.RatingDTO;
import de.pse.oys.dto.auth.AuthType;
import de.pse.oys.dto.auth.LoginDTO;
import de.pse.oys.dto.controller.WrapperDTO;
import de.pse.oys.persistence.CostMatrixRepository;
import de.pse.oys.persistence.LearningUnitRepository;
import de.pse.oys.persistence.ModuleRepository;
import de.pse.oys.persistence.TaskRepository;
import de.pse.oys.persistence.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integrationstests für den RatingController.
 * Überprüft die Bewertung von Lerneinheiten und das Markieren als verpasst.
 */

class RatingControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ModuleRepository moduleRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private LearningUnitRepository learningUnitRepository;
    @Autowired private CostMatrixRepository costMatrixRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    private LearningUnit testUnit;
    private Task testTask;
    private final String rawPassword = "RatingPassword123!";

    @BeforeEach
    void setUp() {
        // 1. User anlegen
        learningUnitRepository.deleteAll();
        costMatrixRepository.deleteAll();
        taskRepository.deleteAll();
        moduleRepository.deleteAll();
        userRepository.deleteAll();

        LocalUser testUser = new LocalUser("ratingUser", passwordEncoder.encode(rawPassword));
        testUser = userRepository.save(testUser);

        // 2. Modul und Task anlegen (Hierarchie: User -> Module -> Task)
        Module module = new Module("Informatik", ModulePriority.MEDIUM);
        module.setUser(testUser);
        module = moduleRepository.save(module);

        testTask = new ExamTask("Programmieren 1", 120, LocalDate.now().plusDays(7));
        testTask.setModule(module);
        testTask = taskRepository.save(testTask);

        // 3. Kostenmatrix anlegen (Wichtig: costs darf nicht null sein)
        // Der RatingService sucht diese Matrix via taskid
        CostMatrix matrix = new CostMatrix("{}", testTask);
        costMatrixRepository.save(matrix);

        // 4. Lerneinheit anlegen (Vergangenheits-Zeitraum für Bewertungsszenario)
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now().minusHours(1);
        testUnit = new LearningUnit(testTask, start, end);
        testUnit = learningUnitRepository.save(testUnit);
    }

    @AfterEach
    void tearDown() {
        learningUnitRepository.deleteAll();
        costMatrixRepository.deleteAll();
        taskRepository.deleteAll();
        moduleRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String getAccessToken() throws Exception {
        LoginDTO login = new LoginDTO();
        login.setAuthType(AuthType.BASIC);
        login.setUsername("ratingUser");
        login.setPassword(rawPassword);

        MvcResult result = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    @DisplayName("POST /api/v1/plan/units/ratings - Erfolgreiche Bewertung einer Unit")
    void testSubmitRating_Success() throws Exception {
        String token = getAccessToken();

        RatingDTO rating = new RatingDTO(
                AchievementLevel.EXCELLENT,
                PerceivedDuration.IDEAL,
                ConcentrationLevel.HIGH
        );

        WrapperDTO<RatingDTO> wrapper = new WrapperDTO<>(testUnit.getUnitId(), rating);

        mockMvc.perform(post("/api/v1/plan/units/ratings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isOk());

        // Verifizieren: Rating vorhanden und CostMatrix als veraltet markiert
        LearningUnit updatedUnit = learningUnitRepository.findById(testUnit.getUnitId()).orElseThrow();
        assertThat(updatedUnit.getRating()).isNotNull();
        assertThat(updatedUnit.getRating().getAchievement()).isEqualTo(AchievementLevel.EXCELLENT);
        assertThat(updatedUnit.getRating().getPerceivedDuration()).isEqualTo(PerceivedDuration.IDEAL);
        assertThat(updatedUnit.getRating().getConcentration()).isEqualTo(ConcentrationLevel.HIGH);

        CostMatrix updatedMatrix = costMatrixRepository.findByTask_TaskId(testTask.getTaskId()).orElseThrow();
        assertThat(updatedMatrix.isOutdated()).isTrue();
    }

    @Test
    @DisplayName("POST /api/v1/plan/units/ratings/missed markiert Einheit als verpasst")
    void testMarkAsMissed() throws Exception {
        String token = getAccessToken();

        WrapperDTO<Void> wrapper = new WrapperDTO<>(testUnit.getUnitId(), null);

        mockMvc.perform(post("/api/v1/plan/units/ratings/missed")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isOk());

        // Status prüfen
        LearningUnit updated = learningUnitRepository.findById(testUnit.getUnitId()).orElseThrow();
        assert(updated.getStatus() == UnitStatus.MISSED);
    }

    @Test
    @DisplayName("GET /api/v1/plan/units/ratings liefert bewertbare Einheiten")
    void testGetRateableUnits() throws Exception {
        String token = getAccessToken();

        mockMvc.perform(get("/api/v1/plan/units/ratings")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0]").value(testUnit.getUnitId().toString()));
    }
}