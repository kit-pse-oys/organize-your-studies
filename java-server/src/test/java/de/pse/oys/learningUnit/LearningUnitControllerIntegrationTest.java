package de.pse.oys.learningUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.pse.oys.domain.ExamTask;
import de.pse.oys.domain.LearningPlan;
import de.pse.oys.domain.LearningUnit;
import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.Module;
import de.pse.oys.domain.Task;
import de.pse.oys.domain.enums.ModulePriority;
import de.pse.oys.dto.auth.AuthType;
import de.pse.oys.dto.auth.LoginDTO;
import de.pse.oys.dto.controller.UnitControlDTO;
import de.pse.oys.persistence.LearningPlanRepository;
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
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@ActiveProfiles("integration")
@AutoConfigureMockMvc
class LearningUnitControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private LearningPlanRepository learningPlanRepository;
    @Autowired private LearningUnitRepository learningUnitRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    private LearningUnit testUnit;
    private final String rawPassword = "PlanPassword123!";
    @Autowired private TaskRepository taskRepository;
    @Autowired private ModuleRepository moduleRepository;

    @BeforeEach
    void setUp() {
        // 1. User anlegen
        LocalUser testUser = new LocalUser("unitUser", passwordEncoder.encode(rawPassword));
        testUser = userRepository.save(testUser);

        // 2. Modul anlegen (Notwendig für Task)
        Module module = new Module("Informatik", ModulePriority.HIGH);
        module.setUser(testUser);
        module = moduleRepository.save(module);

        // 3. Task anlegen (Notwendig für LearningUnit-Konstruktor)
        // Wir nutzen hier ExamTask als konkrete Implementierung
        Task task = new ExamTask("Klausurvorbereitung", 300, LocalDate.now().plusDays(10));
        task.setModule(module);
        task = taskRepository.save(task);

        // 4. LearningPlan anlegen (Neuer Konstruktor: start, end)
        LocalDate start = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDate end = start.plusDays(6);
        LearningPlan plan = new LearningPlan(start, end); // Hier wird der neue Konstruktor genutzt
        plan.setUserId(testUser.getId());
        plan.setUnits(new ArrayList<>());

        // 5. LearningUnit anlegen (Neuer Konstruktor: task, startTime, endTime)
        LocalDateTime unitStart = start.atTime(10, 0);
        LocalDateTime unitEnd = start.atTime(12, 0);
        testUnit = new LearningUnit(task, unitStart, unitEnd); // Hier wird der neue Konstruktor genutzt

        // Wir müssen die Unit zuerst speichern, damit sie nicht mehr "transient" ist
        testUnit = learningUnitRepository.save(testUnit);

        // Verknüpfung und Speichern
        plan.getUnits().add(testUnit);
        learningPlanRepository.save(plan);

        // Die generierte ID für den Test extrahieren
        testUnit = plan.getUnits().get(0);
    }

    @AfterEach
    void tearDown() {
        learningPlanRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String getAccessToken() throws Exception {
        LoginDTO login = new LoginDTO();
        login.setAuthType(AuthType.BASIC);
        login.setUsername("unitUser");
        login.setPassword(rawPassword);

        MvcResult result = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    @DisplayName("POST /api/v1/plan/units/move verschiebt eine Einheit manuell")
    void testMoveLearningUnitManually() throws Exception {
        String token = getAccessToken();
        LocalDateTime newStartTime = LocalDateTime.of(2026, 3, 1, 15, 0);

        UnitControlDTO control = new UnitControlDTO();
        control.setId(testUnit.getUnitId());
        control.setNewTime(newStartTime);

        mockMvc.perform(post("/api/v1/plan/units/move")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(control)))
                .andExpect(status().isOk());

        // Verifikation im Service-Scope: Check ob die Zeit in der DB aktualisiert wurde
        LearningUnit updated = learningUnitRepository.findById(testUnit.getUnitId()).orElseThrow();
        assert(updated.getStartTime().equals(newStartTime));
    }

    @Test
    @DisplayName("POST /api/v1/plan/units/finished markiert Einheit als beendet")
    void testFinishUnitEarly() throws Exception {
        String token = getAccessToken();
        int actualDuration = 45;

        UnitControlDTO control = new UnitControlDTO();
        control.setId(testUnit.getUnitId());
        control.setActualDuration(actualDuration);

        mockMvc.perform(post("/api/v1/plan/units/finished")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(control)))
                .andExpect(status().isOk());

        // Prüfen, ob die tatsächliche Dauer gespeichert wurde
        LearningUnit updated = learningUnitRepository.findById(testUnit.getUnitId()).orElseThrow();
        assert(updated.getActualDurationMinutes() == actualDuration);
    }

    @Test
    @DisplayName("GET /api/v1/plan/units liefert die Einheiten des Nutzers")
    void testGetLearningUnits() throws Exception {
        String token = getAccessToken();

        mockMvc.perform(get("/api/v1/plan/units")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(testUnit.getUnitId().toString()));
    }
}