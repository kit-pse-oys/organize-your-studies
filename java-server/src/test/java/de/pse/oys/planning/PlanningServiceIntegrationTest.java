package de.pse.oys.planning;
import de.pse.oys.domain.*;
import de.pse.oys.domain.Module;
import de.pse.oys.domain.enums.ModulePriority;
import de.pse.oys.domain.enums.TaskStatus;
import de.pse.oys.domain.enums.TimeSlot;
import de.pse.oys.persistence.LearningPlanRepository;
import de.pse.oys.persistence.ModuleRepository;
import de.pse.oys.persistence.TaskRepository;
import de.pse.oys.persistence.UserRepository;
import de.pse.oys.service.planning.PlanningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@SpringBootTest
@Transactional
@Testcontainers
public class PlanningServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    @Container
    static GenericContainer<?> pythonMicroservice = new GenericContainer<>(
            new ImageFromDockerfile()
                    .withFileFromPath(".", Paths.get("../python-microservice"))
    ).withExposedPorts(5001);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("microservice.planning.url", () -> "http://localhost:5001/optimize");

        registry.add("microservice.planning.url",
                () -> "http://" + pythonMicroservice.getHost() + ":" + pythonMicroservice.getMappedPort(5001) + "/optimize");
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @org.springframework.context.annotation.Primary
        public RestTemplate restTemplate() {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(2000);
            factory.setReadTimeout(10000);
            return new RestTemplate(factory);
        }
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlanningService planningService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LearningPlanRepository learningPlanRepository;

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
        LocalDate now = LocalDate.now().plusWeeks(1);



        LocalDateTime startFuture = LocalDate.now().atStartOfDay().plusDays(2);
        LocalDateTime endFuture = startFuture.plusDays(2);

        OtherTask otherTask = new OtherTask("Future Task", 60, startFuture, endFuture);
        otherTask.setModule(module);
        otherTask.setStatus(TaskStatus.OPEN);
        taskRepository.save(otherTask);


        ExamTask task = new ExamTask("TestExamTask", 120, now);
        task.setModule(module);
        task.setWeeklyDurationMinutes(120);
        task.setTitle("TestTask");
        task.setStatus(TaskStatus.OPEN);

        taskRepository.save(task);
        userRepository.flush();
        moduleRepository.flush();
        taskRepository.flush();
    }

    @Test
    void testGenerateWeeklyPlan_EndToEnd() {
        LocalDate weekStart = LocalDate.of(2026, 2, 9);
        System.out.println("Starte Anfrage an Microservice...");

        try {
            planningService.generateWeeklyPlan(savedUser.getId(), weekStart);
        } catch (Exception e) {
            org.junit.jupiter.api.Assertions.fail("Microservice Fehler: " + e.getMessage());
        }

        var plans = learningPlanRepository.findAll();

        if (plans.isEmpty()) {
            System.out.println("ACHTUNG: Keine Pläne gefunden!");
        } else {
            System.out.println("Gefundene Pläne: " + plans.size());
        }

        org.junit.jupiter.api.Assertions.assertFalse(plans.isEmpty(), "Kein Lernplan erstellt!");
        System.out.println("TEST ERFOLGREICH!");
    }
}