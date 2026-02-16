package de.pse.oys.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.Module;
import de.pse.oys.domain.enums.ModulePriority;
import de.pse.oys.domain.enums.TaskCategory;
import de.pse.oys.dto.ExamTaskDTO;
import de.pse.oys.dto.OtherTaskDTO;
import de.pse.oys.dto.SubmissionTaskDTO;
import de.pse.oys.dto.auth.AuthType;
import de.pse.oys.dto.auth.LoginDTO;
import de.pse.oys.persistence.ModuleRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@ActiveProfiles("integration")
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

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
    @Autowired private ModuleRepository moduleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    private Module testModule;
    private final String password = "TestPassword123!";

    @BeforeEach
    void setUp() {
        // 1. User anlegen
        LocalUser testUser = new LocalUser("taskUser", passwordEncoder.encode(password));
        testUser = userRepository.save(testUser);

        // 2. Modul anlegen (ID wird hier automatisch von der DB/Hibernate generiert)
        testModule = new Module("Informatik 1", ModulePriority.HIGH);
        testModule.setUser(testUser);
        testModule = moduleRepository.save(testModule);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    private String getAccessToken() throws Exception {
        LoginDTO login = new LoginDTO();
        login.setAuthType(AuthType.BASIC);
        login.setUsername("taskUser");
        login.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    void testCreateTaskWithGeneratedModuleId() throws Exception {
        String token = getAccessToken();

        // Wir nutzen die ID, die Hibernate beim Speichern in setUp() generiert hat
        UUID actualModuleId = testModule.getModuleId();

        // DTO vorbereiten
        ExamTaskDTO dto = new ExamTaskDTO();
        dto.setCategory(TaskCategory.EXAM);
        dto.setTitle("Mathe Klausur");
        dto.setModuleId(actualModuleId); // Hier setzen wir die echte ID
        dto.setWeeklyTimeLoad(300);
        dto.setExamDate(LocalDate.of(2026, 2, 28));

        // Request absenden
        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testCreateOtherTask_Success() throws Exception {
        String token = getAccessToken();
        UUID actualModuleId = testModule.getModuleId();

        // DTO für eine sonstige Aufgabe (OTHER) vorbereiten
        OtherTaskDTO dto = new OtherTaskDTO();
        dto.setCategory(TaskCategory.OTHER);
        dto.setTitle("Wöchentliche Übung");
        dto.setModuleId(actualModuleId);
        dto.setWeeklyTimeLoad(120);
        dto.setStartTime(LocalDateTime.of(2026, 3, 1, 10, 0));
        dto.setEndTime(LocalDateTime.of(2026, 7, 1, 18, 0));

        // Request durchführen
        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void testCreateSubmissionTask_Success() throws Exception {
        String token = getAccessToken();
        UUID actualModuleId = testModule.getModuleId();

        // DTO für eine Abgabeaufgabe (SUBMISSION) vorbereiten
        SubmissionTaskDTO dto = new SubmissionTaskDTO();
        dto.setCategory(TaskCategory.SUBMISSION);
        dto.setTitle("Projektabgaben");
        dto.setModuleId(actualModuleId);
        dto.setWeeklyTimeLoad(240);
        dto.setFirstDeadline(LocalDateTime.of(2026, 4, 15, 23, 59));
        dto.setSubmissionCycle(2); // Alle 2 Wochen
        dto.setEndTime(LocalDateTime.of(2026, 6, 30, 23, 59));

        // Request durchführen
        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }
}