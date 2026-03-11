package de.pse.oys.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.pse.oys.BaseIntegrationTest;
import de.pse.oys.domain.LearningPreferences;
import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.enums.ModulePriority;
import de.pse.oys.domain.enums.TimeSlot;
import de.pse.oys.dto.ModuleDTO;
import de.pse.oys.dto.auth.AuthType;
import de.pse.oys.dto.auth.LoginDTO;
import de.pse.oys.dto.controller.WrapperDTO;
import de.pse.oys.service.ModuleService;
import de.pse.oys.service.planning.PlanningService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integrationstest für den ModuleController.
 * Nutzt Mocks für die Service-Schicht, um die Controller-Logik isoliert zu prüfen.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ModuleControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ModuleService moduleService;

    @MockBean
    private PlanningService planningService; // Verhindert Solver-Fehler bei updatePlanAfterChange
    private final String password = "TestPassword123!";

    @BeforeEach
    void setUp() {
        // 1. User anlegen
        LocalUser testUser = new LocalUser("taskUser", passwordEncoder.encode(password));
        testUser = userRepository.save(testUser);

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


    @Test
    void testCreateModule_Success() throws Exception {
        String token = getAccessToken();
        UUID generatedId = UUID.randomUUID();

        ModuleDTO dto = new ModuleDTO();
        dto.setTitle("Fortgeschrittene Softwaretechnik");
        dto.setPriority(ModulePriority.HIGH);

        // Mocking: Erstellung liefert die neue ID zurück
        when(moduleService.createModule(any(UUID.class), any(ModuleDTO.class))).thenReturn(generatedId);

        mockMvc.perform(post("/api/v1/modules")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated()) // Erwartet 201 Created
                .andExpect(jsonPath("$.id").value(generatedId.toString()));
    }

    @Test
    void testGetAllModules_Success() throws Exception {
        String token = getAccessToken();
        UUID moduleId = UUID.randomUUID();

        ModuleDTO dto = new ModuleDTO();
        dto.setTitle("Testmodul");
        List<WrapperDTO<ModuleDTO>> modules = List.of(new WrapperDTO<>(moduleId, dto));

        when(moduleService.getModulesByUserId(any(UUID.class))).thenReturn(modules);

        mockMvc.perform(get("/api/v1/modules")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(moduleId.toString()))
                .andExpect(jsonPath("$[0].data.title").value("Testmodul"));
    }

    @Test
    void testUpdateModule_Success() throws Exception {
        String token = getAccessToken();
        UUID moduleId = UUID.randomUUID();

        ModuleDTO dto = new ModuleDTO();
        dto.setTitle("Geändertes Modul");
        dto.setPriority(ModulePriority.MEDIUM);

        // Controller erwartet WrapperDTO für PUT
        WrapperDTO<ModuleDTO> wrapper = new WrapperDTO<>(moduleId, dto);

        mockMvc.perform(put("/api/v1/modules")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isOk());

        verify(moduleService).updateModule(any(UUID.class), any(ModuleDTO.class));
    }

    @Test
    void testDeleteModule_Success() throws Exception {
        String token = getAccessToken();
        UUID moduleId = UUID.randomUUID();

        // Delete erwartet WrapperDTO<Void> mit der ID
        WrapperDTO<Void> wrapper = new WrapperDTO<>(moduleId, null);

        mockMvc.perform(delete("/api/v1/modules")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isOk());

        verify(moduleService).deleteModule(any(UUID.class), eq(moduleId));
    }
}