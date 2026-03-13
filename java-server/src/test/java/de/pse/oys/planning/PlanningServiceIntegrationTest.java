package de.pse.oys.planning;

import de.pse.oys.BaseIntegrationTest;
import de.pse.oys.domain.*;
import de.pse.oys.domain.Module;
import de.pse.oys.domain.enums.ModulePriority;
import de.pse.oys.domain.enums.TimeSlot;
import de.pse.oys.persistence.ModuleRepository;
import de.pse.oys.persistence.TaskRepository;
import de.pse.oys.service.planning.PlanningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PlanningServiceIntegrationTest – Integrationstest für den PlanningService.
 * Nutzt BaseIntegrationTest für Datenbanksetup.
 */
@Transactional
class PlanningServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PlanningService planningService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        LocalUser user = new LocalUser("TestUser", "password");
        LearningPreferences preferences = new LearningPreferences(
                30, 180, 8, 15, 1,
                Set.of(TimeSlot.MORNING, TimeSlot.FORENOON, TimeSlot.AFTERNOON),
                Set.of(DayOfWeek.values())
        );
        user.setPreferences(preferences);
        savedUser = userRepository.save(user);

        Module module = new Module("TestModule", ModulePriority.HIGH);
        module.setUser(savedUser);
        module = moduleRepository.save(module);

        LocalDateTime startFuture = LocalDate.now().atStartOfDay().plusDays(2);
        LocalDateTime endFuture = startFuture.plusDays(2);
        OtherTask otherTask = new OtherTask("Future Task", 60, startFuture, endFuture);
        otherTask.setModule(module);
        taskRepository.save(otherTask);

        LocalDate deadlineDate = LocalDate.now().plusWeeks(1);
        ExamTask task = new ExamTask("TestExamTask", 120, deadlineDate);
        task.setModule(module);
        task.setWeeklyDurationMinutes(120);
        taskRepository.save(task);

        // Flushen der Repositories, um sicherzustellen, dass alle Daten in der Datenbank sind, bevor die Tests ausgeführt werden
        userRepository.flush();
        moduleRepository.flush();
        taskRepository.flush();
    }

    @Test
    void testGenerateWeeklyPlan_Execution() {
        assertNotNull(savedUser, "Nutzer sollte gespeichert sein");
        assertNotNull(savedUser.getId(), "Nutzer-ID sollte existieren");
        assertFalse(taskRepository.findAll().isEmpty(), "Es sollten Aufgaben existieren");

        assertDoesNotThrow(() -> planningService.generateWeeklyPlan(savedUser.getId())); // Keine Ausnahme sollte geworfen werden
    }
}