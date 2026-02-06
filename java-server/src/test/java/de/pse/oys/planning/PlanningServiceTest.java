package de.pse.oys.planning;

import de.pse.oys.domain.*;
import de.pse.oys.domain.enums.TaskCategory;
import de.pse.oys.domain.enums.TimeSlot;
import de.pse.oys.dto.plan.PlanningRequestDTO;
import de.pse.oys.dto.plan.PlanningResponseDTO;
import de.pse.oys.dto.plan.PlanningTaskDTO;
import de.pse.oys.persistence.CostMatrixRepository;
import de.pse.oys.persistence.LearningPlanRepository;
import de.pse.oys.persistence.TaskRepository;
import de.pse.oys.persistence.UserRepository;
import de.pse.oys.service.planning.LearningAnalyticsProvider;
import de.pse.oys.service.planning.PlanningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PlanningServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private LearningPlanRepository learningPlanRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CostMatrixRepository costMatrixRepository;
    @Mock
    private LearningAnalyticsProvider learningAnalyticsProvider;
    @Mock
    private RestTemplate restTemplate;

    private PlanningService planningService;

    @Captor
    private ArgumentCaptor<HttpEntity<PlanningRequestDTO>> requestCaptor;


    @Mock
    private User testUser;
    @Mock
    private Task testTask;
    @Mock
    private LearningPreferences testPreferences; // Auch das ist jetzt ein Mock

    private final UUID userId = UUID.randomUUID();
    private final UUID taskId = UUID.randomUUID();

    /*** --- SETUP --- */
    @BeforeEach
    void setUp() {

        planningService = new PlanningService(
                taskRepository,
                learningPlanRepository,
                userRepository,
                learningAnalyticsProvider,
                restTemplate
        );

        // URL setzen
        ReflectionTestUtils.setField(planningService, "planningMicroserviceUrl", "http://localhost:5000/solve");

        // --- 1. LEARNING PREFERENCES MOCKEN ---

        lenient().when(testPreferences.getMinUnitDurationMinutes()).thenReturn(30);
        lenient().when(testPreferences.getMaxUnitDurationMinutes()).thenReturn(90);
        lenient().when(testPreferences.getBreakDurationMinutes()).thenReturn(15);
        lenient().when(testPreferences.getDeadlineBufferDays()).thenReturn(1);
        lenient().when(testPreferences.getPreferredTimeSlots()).thenReturn(Set.of(TimeSlot.MORNING));
        lenient().when(testPreferences.getPreferredDays()).thenReturn(Set.of(DayOfWeek.values()));

        // --- 2. USER MOCKEN ---

        lenient().when(testUser.getId()).thenReturn(userId);
        lenient().when(testUser.getPreferences()).thenReturn(testPreferences);
        lenient().when(testUser.getFreeTimes()).thenReturn(new ArrayList<>());

        // --- 3. TASK MOCKEN ---

        lenient().when(testTask.isActive()).thenReturn(true);
        lenient().when(testTask.getTaskId()).thenReturn(taskId);
        lenient().when(testTask.getWeeklyDurationMinutes()).thenReturn(120);
        lenient().when(testTask.getCategory()).thenReturn(TaskCategory.EXAM);
        lenient().when(testTask.getLearningUnits()).thenReturn(new ArrayList<>());
        lenient().when(testTask.getSoftDeadline(anyInt()))
                .thenReturn(LocalDate.of(2026, 1, 30).atTime(12, 0));

    }

    /*** --- TESTS --- */

    @Test
    /** Testet, ob der wöchentliche Plan korrekt generiert wird und die richtige Anfrage an den Solver gesendet wird.
     */
    void generateWeeklyPlan_ShouldSendCorrectRequest() {
        LocalDate weekStart = LocalDate.of(2026, 1, 26);


        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(taskRepository.findAllByModuleUserUserId(eq(userId)))
                .thenReturn(List.of(testTask));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(learningAnalyticsProvider.getCostMatrixForTask(any())).thenReturn(Collections.emptyList());


        PlanningResponseDTO responseItem = new PlanningResponseDTO();
        responseItem.setId(taskId.toString() + "_0");
        responseItem.setStart(72); // 06:00 Uhr
        responseItem.setEnd(87);   // 07:00 Uhr + pause


        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(List.of(responseItem)));


        planningService.generateWeeklyPlan(userId, weekStart);


        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                requestCaptor.capture(),
                any(ParameterizedTypeReference.class)
        );

        PlanningRequestDTO request = requestCaptor.getValue().getBody();

        assertNotNull(request);
        assertEquals(2016, request.getHorizon());
        assertFalse(request.getTasks().isEmpty());


        assertTrue(request.getPreferredSlots().contains(TimeSlot.MORNING.toString()));

        verify(learningPlanRepository).save(any(LearningPlan.class));
    }

    @Test
    void generateWeeklyPlan_ShouldAddPaddingToRequest_AndRemoveItFromPersistence() {
        LocalDate weekStart = LocalDate.of(2026, 1, 26);

        lenient().when(testPreferences.getBreakDurationMinutes()).thenReturn(15);


        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(taskRepository.findAllByModuleUserUserId(eq(userId)))
                .thenReturn(List.of(testTask));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask)); // Fürs Speichern
        when(learningAnalyticsProvider.getCostMatrixForTask(any())).thenReturn(Collections.emptyList());


        PlanningResponseDTO responseItem = new PlanningResponseDTO();
        responseItem.setId(taskId.toString() + "_0");
        responseItem.setStart(72);
        responseItem.setEnd(87);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(List.of(responseItem)));


        planningService.generateWeeklyPlan(userId, weekStart);


        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                requestCaptor.capture(),
                any(ParameterizedTypeReference.class)
        );
        PlanningRequestDTO request = requestCaptor.getValue().getBody();


        PlanningTaskDTO sentTask = request.getTasks().get(0);

        assertTrue(sentTask.getDuration() >= 15,
                "Der Request an Python muss die Pause beinhalten (mind. 15 Slots bei 75min)");

        ArgumentCaptor<LearningPlan> planCaptor = ArgumentCaptor.forClass(LearningPlan.class);
        verify(learningPlanRepository).save(planCaptor.capture());

        LearningUnit savedUnit = planCaptor.getValue().getUnits().get(0);


        assertEquals(LocalDateTime.of(weekStart, LocalTime.of(6, 0)), savedUnit.getStartTime());


        LocalDateTime expectedEnd = LocalDateTime.of(weekStart, LocalTime.of(7, 0));

        assertEquals(expectedEnd, savedUnit.getEndTime(),
                "In der DB darf die Pause NICHT enthalten sein. Ende muss 07:00 sein, nicht 07:15.");
    }


    @Test
    /** Testet die Verschiebung einer Lerneinheit und überprüft, ob die Anfrage an den Solver korrekt ist.
     */
    void rescheduleUnit_ShouldWorkWithMocks() {
        LocalDate weekStart = LocalDate.of(2026, 1, 26);
        UUID unitIdToMove = UUID.randomUUID();
        UUID otherUnitId = UUID.randomUUID();


        LearningUnit unitToMoveMock = mock(LearningUnit.class);

        lenient().when(unitToMoveMock.getUnitId()).thenReturn(unitIdToMove);
        lenient().when(unitToMoveMock.getTask()).thenReturn(testTask);
        lenient().when(unitToMoveMock.getStartTime()).thenReturn(weekStart.atTime(10, 0));
        lenient().when(unitToMoveMock.getEndTime()).thenReturn(weekStart.atTime(11, 0));

        LearningUnit otherUnitMock = mock(LearningUnit.class);


        lenient().when(otherUnitMock.getUnitId()).thenReturn(otherUnitId);


        lenient().when(otherUnitMock.getStartTime()).thenReturn(weekStart.atTime(12, 0));
        lenient().when(otherUnitMock.getEndTime()).thenReturn(weekStart.atTime(13, 0));


        LearningPlan existingPlanMock = mock(LearningPlan.class);
        when(existingPlanMock.getUnits()).thenReturn(List.of(unitToMoveMock, otherUnitMock));


        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(learningPlanRepository.findByUserIdAndWeekStart(eq(unitIdToMove), eq(weekStart)))
                .thenReturn(Optional.of(existingPlanMock));

        when(learningAnalyticsProvider.getCostMatrixForTask(any())).thenReturn(Collections.emptyList());


        PlanningResponseDTO response = new PlanningResponseDTO();
        response.setStart(200);
        response.setEnd(212);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(List.of(response)));


        planningService.rescheduleUnit(userId, weekStart, unitIdToMove);


        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                requestCaptor.capture(),
                any(ParameterizedTypeReference.class)
        );

        PlanningRequestDTO request = requestCaptor.getValue().getBody();


        boolean otherUnitBlocked = request.getFixedBlocks().stream()
                .anyMatch(b -> b.getStart() == 144);
        assertTrue(otherUnitBlocked);


        verify(unitToMoveMock).setStartTime(any());
        verify(unitToMoveMock).setEndTime(any());

        verify(learningPlanRepository).save(existingPlanMock);
    }

    @Test
    void rescheduleUnit_ShouldForceMoveAndLearnPreferences() {
        LocalDate weekStart = LocalDate.of(2026, 1, 26);
        UUID unitIdToMove = UUID.randomUUID();


        Task taskMock = mock(Task.class);
        when(taskMock.getTaskId()).thenReturn(taskId);

        when(taskMock.getSoftDeadline(anyInt())).thenReturn(weekStart.plusDays(4).atTime(12, 0));


        LearningUnit unitToMoveMock = mock(LearningUnit.class);
        lenient().when(unitToMoveMock.getUnitId()).thenReturn(unitIdToMove);
        lenient().when(unitToMoveMock.getStartTime()).thenReturn(weekStart.atTime(10, 0));
        lenient().when(unitToMoveMock.getEndTime()).thenReturn(weekStart.atTime(11, 0));
        lenient().when(unitToMoveMock.getTask()).thenReturn(taskMock);


        LearningPlan existingPlanMock = mock(LearningPlan.class);
        lenient().when(existingPlanMock.getUnits()).thenReturn(List.of(unitToMoveMock));


        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(learningPlanRepository.findByUserIdAndWeekStart(eq(unitIdToMove), eq(weekStart)))
                .thenReturn(Optional.of(existingPlanMock));

        when(learningAnalyticsProvider.getCostMatrixForTask(any())).thenReturn(Collections.emptyList());


        PlanningResponseDTO response = new PlanningResponseDTO();
        response.setStart(200);
        response.setEnd(212);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(ResponseEntity.ok(List.of(response)));


        planningService.rescheduleUnit(userId, weekStart, unitIdToMove);


        verify(learningAnalyticsProvider).applyPenaltyToCostMatrix(eq(taskMock), eq(120), eq(10));


        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                requestCaptor.capture(),
                any(ParameterizedTypeReference.class)
        );

        PlanningRequestDTO request = requestCaptor.getValue().getBody();

        boolean oldSlotBlocked = request.getFixedBlocks().stream()
                .anyMatch(b -> b.getStart() == 120);

        assertTrue(oldSlotBlocked,
                "Der alte Slot (10:00 Uhr) MUSS blockiert sein, damit der Solver zwingend verschiebt!");


        verify(unitToMoveMock).setStartTime(any());
        verify(learningPlanRepository).save(existingPlanMock);
    }
}