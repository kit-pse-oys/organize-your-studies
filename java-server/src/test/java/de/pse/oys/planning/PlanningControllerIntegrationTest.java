package de.pse.oys.planning;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.pse.oys.BaseIntegrationTest;
import de.pse.oys.domain.LearningPreferences;
import de.pse.oys.domain.LocalUser;
import de.pse.oys.domain.enums.TimeSlot;
import de.pse.oys.dto.UnitDTO;
import de.pse.oys.dto.auth.AuthType;
import de.pse.oys.dto.auth.LoginDTO;
import de.pse.oys.dto.controller.WrapperDTO;
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
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlanningControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlanningService planningService;

    private final String password = "TestPassword123!";

    @BeforeEach
    void setUp() {
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
    void testGenerateWeeklyPlan_Success() throws Exception {
        String token = getAccessToken();

        // PUT /api/v1/plan anstoßen
        mockMvc.perform(put("/api/v1/plan")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Verifizieren, dass der Service mit einer UUID aufgerufen wurde
        verify(planningService).generateWeeklyPlan(any(UUID.class));
    }

    @Test
    void testRescheduleUnit_Success() throws Exception {
        String token = getAccessToken();
        UUID unitId = UUID.randomUUID();

        // Mock-Antwort vorbereiten
        UnitDTO unitDTO = new UnitDTO();

        // Service-Mock konfigurieren
        when(planningService.rescheduleUnit(any(UUID.class), eq(unitId)))
                .thenReturn(unitDTO);

        // Request-Body (WrapperDTO<Void> laut Controller)
        WrapperDTO<Void> requestBody = new WrapperDTO<>(unitId, null);

        mockMvc.perform(post("/api/v1/plan/units/moveAuto")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(unitId.toString()));

        verify(planningService).rescheduleUnit(any(UUID.class), eq(unitId));
    }
}