package de.pse.oys.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.pse.oys.BaseIntegrationTest;
import de.pse.oys.domain.LearningPreferences;
import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.Module;
import de.pse.oys.domain.enums.ModulePriority;
import de.pse.oys.domain.enums.TaskCategory;
import de.pse.oys.domain.enums.TimeSlot;
import de.pse.oys.dto.ExamTaskDTO;
import de.pse.oys.dto.OtherTaskDTO;
import de.pse.oys.dto.SubmissionTaskDTO;
import de.pse.oys.dto.TaskDTO;
import de.pse.oys.dto.auth.AuthType;
import de.pse.oys.dto.auth.LoginDTO;
import de.pse.oys.dto.controller.WrapperDTO;
import de.pse.oys.persistence.ModuleRepository;
import de.pse.oys.persistence.UserRepository;
import de.pse.oys.service.TaskService;
import de.pse.oys.service.exception.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class TaskControllerIntegrationTest extends BaseIntegrationTest {
    @MockBean
    private TaskService taskService;

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ModuleRepository moduleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    private Module testModule;
    private final String password = "TestPassword123!";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // 1. User anlegen
        LocalUser testUser = new LocalUser("taskUser", passwordEncoder.encode(password));
        testUser = userRepository.save(testUser);

        // 2. Modul anlegen (ID wird hier automatisch von der DB/Hibernate generiert)
        testModule = new Module("Informatik 1", ModulePriority.HIGH);
        testModule.setUser(testUser);
        testModule = moduleRepository.save(testModule);

        LearningPreferences prefs = new LearningPreferences(
                30,                          // minUnitDurationMinutes
                90,                          // maxUnitDurationMinutes
                8,                           // maxDailyWorkloadHours
                15,                          // breakDurationMinutes
                1,                           // deadlineBufferDays
                Set.of(TimeSlot.MORNING),     // preferredTimeSlots
                Set.of(DayOfWeek.values())   // preferredDays
        );

        testUser.setPreferences(prefs); // Präferenzen dem User zuweisen
        userRepository.save(testUser);
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

    private OtherTaskDTO createDefaultOtherTaskDTO(UUID moduleId) {
        OtherTaskDTO dto = new OtherTaskDTO();
        dto.setCategory(TaskCategory.OTHER);
        dto.setTitle("Standard Testaufgabe");
        dto.setModuleId(moduleId);
        dto.setWeeklyTimeLoad(60);
        dto.setStartTime(LocalDateTime.now().plusDays(1));
        dto.setEndTime(LocalDateTime.now().plusWeeks(1));
        return dto;
    }

    @Test
    void testCreateTaskWithGeneratedModuleId() throws Exception {
        String token = getAccessToken();
        UUID generatedId = UUID.randomUUID();

        // WICHTIG: Rückgabewert für den Mock definieren
        when(taskService.createTask(any(), any())).thenReturn(generatedId);

        ExamTaskDTO dto = new ExamTaskDTO();
        dto.setCategory(TaskCategory.EXAM); // WICHTIG für Jackson Polymorphismus
        dto.setTitle("Mathe Klausur");
        dto.setModuleId(testModule.getModuleId());
        dto.setWeeklyTimeLoad(300);
        dto.setExamDate(LocalDate.of(2026, 2, 28));

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(generatedId.toString()));
    }

    @Test
    void testCreateOtherTask_Success() throws Exception {
        String token = getAccessToken();
        UUID actualModuleId = testModule.getModuleId();
        UUID generatedId = UUID.randomUUID();

        // WICHTIG: Rückgabewert für den Mock definieren
        when(taskService.createTask(any(), any())).thenReturn(generatedId);

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
        UUID generatedId = UUID.randomUUID();

        // WICHTIG: Rückgabewert für den Mock definieren
        when(taskService.createTask(any(), any())).thenReturn(generatedId);

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

    @Test
    void testGetTasks_Success() throws Exception {
        String token = getAccessToken();
        UUID taskId = UUID.randomUUID();
        OtherTaskDTO taskDTO = createDefaultOtherTaskDTO(testModule.getModuleId());

        // Wir bauen die Antwortstruktur des Services nach
        List<WrapperDTO<TaskDTO>> serviceResponse = List.of(new WrapperDTO<>(taskId, taskDTO));
        when(taskService.getTasksByUserId(any(UUID.class))).thenReturn(serviceResponse);

        mockMvc.perform(get("/api/v1/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].data.title").value("Standard Testaufgabe"));
    }

    @Test
    void testUpdateTask_Success() throws Exception {
        String token = getAccessToken();
        UUID taskId = UUID.randomUUID();

        // Vorbereiten des WrapperDTOs für das Update
        OtherTaskDTO updatedData = createDefaultOtherTaskDTO(testModule.getModuleId());
        updatedData.setTitle("Aktualisierter Titel");

        when(taskService.updateTask(any(UUID.class), eq(taskId), any(TaskDTO.class)))
                .thenReturn(taskId);

        WrapperDTO<TaskDTO> wrapper = new WrapperDTO<>(taskId, updatedData);

        mockMvc.perform(put("/api/v1/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isOk());

        verify(taskService).updateTask(any(UUID.class), eq(taskId), any(TaskDTO.class));
    }

    @Test
    void testDeleteTask_Success() throws Exception {
        String token = getAccessToken();
        UUID taskId = UUID.randomUUID();
        WrapperDTO<TaskDTO> deleteWrapper = new WrapperDTO<>(taskId, null);

        mockMvc.perform(delete("/api/v1/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteWrapper)))
                .andExpect(status().isNoContent());

        // Verifiziert: deleteTask(UUID userId, UUID taskId)
        verify(taskService).deleteTask(any(UUID.class), eq(taskId));
    }

    @Test
    void testUpdateTask_ValidationError() throws Exception {
        String token = getAccessToken();
        UUID taskId = UUID.randomUUID();

        // Wir senden ein valides DTO (inkl. Category), um das Parsing zu ermöglichen
        OtherTaskDTO data = createDefaultOtherTaskDTO(testModule.getModuleId());
        WrapperDTO<TaskDTO> wrapper = new WrapperDTO<>(taskId, data);

        doThrow(new ValidationException("Validierung fehlgeschlagen"))
                .when(taskService).updateTask(any(), any(), any());

        mockMvc.perform(put("/api/v1/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Validierung fehlgeschlagen"));
    }

    @Test
    void testCreateTask_MockedService_Success() throws Exception {
        String token = getAccessToken();
        UUID generatedId = UUID.randomUUID();
        OtherTaskDTO dto = createDefaultOtherTaskDTO(testModule.getModuleId());

        // Der Service gibt die neue UUID zurück
        when(taskService.createTask(any(UUID.class), any(TaskDTO.class))).thenReturn(generatedId);

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(generatedId.toString())); // Validiert das Map-Padding des Controllers
    }
}