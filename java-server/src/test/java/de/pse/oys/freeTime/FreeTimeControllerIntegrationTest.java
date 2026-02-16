package de.pse.oys.freeTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.pse.oys.domain.LocalUser;
import de.pse.oys.dto.FreeTimeDTO;
import de.pse.oys.dto.auth.AuthType;
import de.pse.oys.dto.auth.LoginDTO;
import de.pse.oys.dto.controller.WrapperDTO;
import de.pse.oys.persistence.FreeTimeRepository;
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
import java.time.LocalTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integrationstest für den FreeTimeController.
 * Testet die Endpunkte für das Abrufen, Erstellen, Aktualisieren und Löschen von Freizeitblöcken.
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("integration")
@AutoConfigureMockMvc
class FreeTimeControllerIntegrationTest {

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
    @Autowired private FreeTimeRepository freeTimeRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    private final String rawPassword = "FreeTimePass123!";

    @BeforeEach
    void setUp() {
        LocalUser testUser = new LocalUser("freeUser", passwordEncoder.encode(rawPassword));
        userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        freeTimeRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Hilfsmethode, um einen gültigen JWT-Token für den Testuser zu erhalten.
     */
    private String getAccessToken() throws Exception {
        LoginDTO login = new LoginDTO();
        login.setAuthType(AuthType.BASIC);
        login.setUsername("freeUser");
        login.setPassword(rawPassword);

        MvcResult result = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    @DisplayName("Vollständiger Lebenszyklus eines Freizeitblocks: CREATE, QUERY, UPDATE, DELETE")
    void testFullFreeTimeCycle() throws Exception {
        String token = getAccessToken();
        String authHeader = "Bearer " + token;

        // 1. CREATE: Einmaliger Freizeitblock
        FreeTimeDTO createDto = new FreeTimeDTO();
        createDto.setTitle("Mittagspause");
        createDto.setDate(LocalDate.of(2026, 5, 20));
        createDto.setStartTime(LocalTime.of(12, 0));
        createDto.setEndTime(LocalTime.of(13, 0));
        createDto.setWeekly(false);

        MvcResult createResult = mockMvc.perform(post("/api/v1/freeTimes")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        UUID freeTimeId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText());

        // 2. QUERY: Prüfen, ob der Block in der Liste erscheint
        mockMvc.perform(get("/api/v1/freeTimes")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(freeTimeId.toString()))
                .andExpect(jsonPath("$[0].data.title").value("Mittagspause"));

        // 3. UPDATE: Zeit anpassen (innerhalb des WrapperDTO)
        createDto.setTitle("Lange Mittagspause");
        createDto.setEndTime(LocalTime.of(14, 0));
        WrapperDTO<FreeTimeDTO> updateWrapper = new WrapperDTO<>(freeTimeId, createDto);

        mockMvc.perform(put("/api/v1/freeTimes")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateWrapper)))
                .andExpect(status().isOk());

        // 4. DELETE: Freizeit wieder entfernen
        WrapperDTO<Void> deleteWrapper = new WrapperDTO<>(freeTimeId, null);
        mockMvc.perform(delete("/api/v1/freeTimes")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteWrapper)))
                .andExpect(status().isNoContent());

        // Verifizierung: Liste muss leer sein
        mockMvc.perform(get("/api/v1/freeTimes")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("POST /api/v1/freeTimes mit überlappenden Zeiten sollte fehlschlagen")
    void testCreateWeeklyOverlap_ShouldFail() throws Exception {
        String token = getAccessToken();
        String authHeader = "Bearer " + token;

        // Erster Block: Montags 08:00 - 10:00 (Wöchentlich)
        FreeTimeDTO ft1 = new FreeTimeDTO();
        ft1.setTitle("Vorlesung");
        ft1.setDate(LocalDate.of(2026, 1, 5)); // Ein Montag
        ft1.setStartTime(LocalTime.of(8, 0));
        ft1.setEndTime(LocalTime.of(10, 0));
        ft1.setWeekly(true);

        mockMvc.perform(post("/api/v1/freeTimes")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ft1)))
                .andExpect(status().isOk());

        // Zweiter Block am selben Montag: 09:00 - 11:00 -> Überschneidung
        FreeTimeDTO ft2 = new FreeTimeDTO();
        ft2.setTitle("Überlappend");
        ft2.setDate(LocalDate.of(2026, 1, 5));
        ft2.setStartTime(LocalTime.of(9, 0));
        ft2.setEndTime(LocalTime.of(11, 0));
        ft2.setWeekly(false);

        mockMvc.perform(post("/api/v1/freeTimes")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ft2)))
                .andExpect(status().isBadRequest()) // ValidationException -> 400
                .andExpect(content().string("Die Freizeit überschneidet sich mit einem bestehenden Eintrag."));
    }

    @Test
    @DisplayName("GET /api/v1/freeTimes liefert alle Einträge des Nutzers")
    void testQueryFreeTimes() throws Exception {
        String token = getAccessToken();

        // Vorab einen Eintrag über den Service oder Controller erstellen
        FreeTimeDTO ft = new FreeTimeDTO();
        ft.setTitle("Sport");
        ft.setDate(LocalDate.now());
        ft.setStartTime(LocalTime.of(18, 0));
        ft.setEndTime(LocalTime.of(19, 0));
        ft.setWeekly(false);

        mockMvc.perform(post("/api/v1/freeTimes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ft)))
                .andExpect(status().isOk());

        // Abfrage aller Einträge
        mockMvc.perform(get("/api/v1/freeTimes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].data.title").value("Sport"));
    }

    @Test
    @DisplayName("PUT /api/v1/freeTimes aktualisiert einen bestehenden Eintrag")
    void testUpdateFreeTime() throws Exception {
        String token = getAccessToken();

        // 1. Erstellen
        FreeTimeDTO ft = new FreeTimeDTO();
        ft.setTitle("Lernen");
        ft.setDate(LocalDate.now());
        ft.setStartTime(LocalTime.of(10, 0));
        ft.setEndTime(LocalTime.of(11, 0));
        ft.setWeekly(true);

        MvcResult createResult = mockMvc.perform(post("/api/v1/freeTimes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ft)))
                .andExpect(status().isOk())
                .andReturn();

        UUID id = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText());

        // 2. Update vorbereiten (WrapperDTO nutzen)
        ft.setTitle("Intensiv Lernen");
        ft.setEndTime(LocalTime.of(12, 0));
        WrapperDTO<FreeTimeDTO> wrapper = new WrapperDTO<>(id, ft);

        mockMvc.perform(put("/api/v1/freeTimes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isOk());

        // Prüfen, ob die Änderung übernommen wurde
        mockMvc.perform(get("/api/v1/freeTimes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].data.title").value("Intensiv Lernen"))
                .andExpect(jsonPath("$[0].data.endTime").value("12:00:00"));
    }

    @Test
    @DisplayName("DELETE /api/v1/freeTimes löscht einen Eintrag")
    void testDeleteFreeTime() throws Exception {
        String token = getAccessToken();

        // 1. Erstellen
        FreeTimeDTO ft = new FreeTimeDTO();
        ft.setTitle("Zu löschen");
        ft.setDate(LocalDate.now());
        ft.setStartTime(LocalTime.of(14, 0));
        ft.setEndTime(LocalTime.of(15, 0));
        ft.setWeekly(false);

        MvcResult createResult = mockMvc.perform(post("/api/v1/freeTimes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ft)))
                .andExpect(status().isOk())
                .andReturn();

        UUID id = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText());

        // 2. Löschen (WrapperDTO mit ID wird erwartet)
        WrapperDTO<Void> deleteWrapper = new WrapperDTO<>(id, null);

        mockMvc.perform(delete("/api/v1/freeTimes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteWrapper)))
                .andExpect(status().isNoContent()); // 204 No Content erwartet

        // Prüfen, ob die Liste nun leer ist
        mockMvc.perform(get("/api/v1/freeTimes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}