package de.pse.oys.planning;

import de.pse.oys.domain.*;
import de.pse.oys.domain.enums.TaskCategory;
import de.pse.oys.domain.enums.TimeSlot;
import de.pse.oys.dto.plan.PlanningRequestDTO;
import de.pse.oys.dto.plan.PlanningResponseDTO;
import de.pse.oys.dto.plan.PlanningTaskDTO;
import de.pse.oys.persistence.*;
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
    private LearningUnitRepository learningUnitRepository;
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
    private LearningPreferences testPreferences;

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
                restTemplate,
                learningUnitRepository
        );


        ReflectionTestUtils.setField(planningService, "planningMicroserviceUrl", "http://localhost:5001/optimize");

        //Learning preferences

        lenient().when(testPreferences.getMinUnitDurationMinutes()).thenReturn(30);
        lenient().when(testPreferences.getMaxUnitDurationMinutes()).thenReturn(90);
        lenient().when(testPreferences.getBreakDurationMinutes()).thenReturn(15);
        lenient().when(testPreferences.getDeadlineBufferDays()).thenReturn(1);
        lenient().when(testPreferences.getPreferredTimeSlots()).thenReturn(Set.of(TimeSlot.MORNING));
        lenient().when(testPreferences.getPreferredDays()).thenReturn(Set.of(DayOfWeek.values()));

        // User mock

        lenient().when(testUser.getId()).thenReturn(userId);
        lenient().when(testUser.getPreferences()).thenReturn(testPreferences);
        lenient().when(testUser.getFreeTimes()).thenReturn(new ArrayList<>());

        // Task mock

        lenient().when(testTask.isActive()).thenReturn(true);
        lenient().when(testTask.getTaskId()).thenReturn(taskId);
        lenient().when(testTask.getWeeklyDurationMinutes()).thenReturn(120);
        lenient().when(testTask.getCategory()).thenReturn(TaskCategory.EXAM);
        lenient().when(testTask.getLearningUnits()).thenReturn(new ArrayList<>());
        lenient().when(testTask.getSoftDeadline(anyInt()))
                .thenReturn(LocalDate.of(2026, 1, 30).atTime(12, 0));

        // Repos Mock
        lenient().when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        lenient().when(taskRepository.saveAndFlush(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        lenient().when(learningPlanRepository.save(any(LearningPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

    }

    /*** --- TESTS --- */

    @Test
    /** Testet, ob der wöchentliche Plan korrekt generiert wird und die richtige Anfrage an den Solver gesendet wird.
     */
    void generateWeeklyPlan_ShouldSendCorrectRequest() {

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(taskRepository.findAllByModuleUserUserId(eq(userId)))
                .thenReturn(List.of(testTask));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(learningAnalyticsProvider.getCostMatrixForTask(any())).thenReturn(Collections.emptyList());


        PlanningResponseDTO responseItem = new PlanningResponseDTO();
        responseItem.setId(taskId+ "_0");
        responseItem.setStart(72); // 06:00 Uhr
        responseItem.setEnd(87);   // 07:00 Uhr + pause


        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(List.of(responseItem)));


        planningService.generateWeeklyPlan(userId);


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


        assertTrue(request.getPreference_time().contains(TimeSlot.MORNING.toString()));

        verify(learningPlanRepository, times(1)).save(any(LearningPlan.class));
    }
    /** Testet, ob die Pause korrekt in die Anfrage an den Solver integriert wird und nicht in der persistierten Lerneinheit bleibt.
     */

    @Test
    void generateWeeklyPlan_ShouldAddPaddingToRequest_AndRemoveItFromPersistence() {
        LocalDate weekStart = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        lenient().when(testPreferences.getBreakDurationMinutes()).thenReturn(15);

        LocalDate futureExamDate = weekStart.plusWeeks(4);
        ExamTask realTask = new ExamTask("Real Test Task", 120, futureExamDate);
        ReflectionTestUtils.setField(realTask, "taskId", taskId);

        realTask.isActive();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(taskRepository.findAllByModuleUserUserId(eq(userId)))
                .thenReturn(List.of(realTask));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(realTask));
        when(learningAnalyticsProvider.getCostMatrixForTask(any())).thenReturn(Collections.emptyList());


        PlanningResponseDTO responseItem = new PlanningResponseDTO();
        responseItem.setId(taskId + "_0");
        responseItem.setStart(72);
        responseItem.setEnd(87);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(List.of(responseItem)));


        planningService.generateWeeklyPlan(userId);


        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                requestCaptor.capture(),
                any(ParameterizedTypeReference.class)
        );
        PlanningRequestDTO request = requestCaptor.getValue().getBody();


        PlanningTaskDTO sentTask = request.getTasks().get(0);

        assertTrue(sentTask.getDuration() >= 15,
                "Der Request an Python muss die Pause beinhalten mind. 15 Slots bei 75min");

        ArgumentCaptor<LearningPlan> planCaptor = ArgumentCaptor.forClass(LearningPlan.class);

        verify(learningPlanRepository, atLeast(1)).save(planCaptor.capture());

        List<LearningPlan> capturedPlans = planCaptor.getAllValues();
        LearningPlan finalPlan = capturedPlans.get(capturedPlans.size() - 1);

        assertFalse(finalPlan.getUnits().isEmpty(), "Die Liste der Units darf nicht leer sein");
        LearningUnit savedUnit = finalPlan.getUnits().get(0);
        assertEquals(LocalDateTime.of(weekStart, LocalTime.of(6, 0)), savedUnit.getStartTime());


        LocalDateTime expectedEnd = LocalDateTime.of(weekStart, LocalTime.of(7, 0));

        assertEquals(expectedEnd, savedUnit.getEndTime(),
                "In der DB darf die Pause nicht enthalten sein. Ende muss 07:00 sein");
    }


    @Test
    /** Testet die Verschiebung einer Lerneinheit und überprüft, ob die Anfrage an den Solver korrekt ist.
     */
    void rescheduleUnit_ShouldWorkWithMocks() {
        LocalDate weekStart = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
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
        when(learningPlanRepository.findByUserIdAndWeekStart(eq(userId), eq(weekStart)))
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

        planningService.rescheduleUnit(userId, unitIdToMove);


        verify(restTemplate).exchange(


                anyString(),
                eq(HttpMethod.POST),
                requestCaptor.capture(),
                any(ParameterizedTypeReference.class)

        );

        PlanningRequestDTO request = requestCaptor.getValue().getBody();

        boolean isBlocked = false;
        for (var block : request.getFixed_blocks()) {
            if (block.getStart() == 144) {
                isBlocked = true;
                break;
            }
        }
        assertTrue(isBlocked);


        verify(unitToMoveMock).setStartTime(any());
        verify(unitToMoveMock).setEndTime(any());

        verify(learningPlanRepository).save(existingPlanMock);
    }

    /**
     * Testet, ob bei der Verschiebung einer Lerneinheit die alten Zeiten blockiert werden, um eine zwingende Verschiebung zu erzwingen.
        * Außerdem wird überprüft, ob die Kostenmatrix mit einer Strafe für den alten Slot aktualisiert wird.
     */
    @Test
    void rescheduleUnit_ShouldForceMoveAndLearnPreferences() {
        LocalDate weekStart = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
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

        when(learningPlanRepository.findByUserIdAndWeekStart(eq(userId), eq(weekStart)))
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


        planningService.rescheduleUnit(userId, unitIdToMove);


        verify(learningAnalyticsProvider).applyPenaltyToCostMatrix(eq(taskMock), eq(120), eq(10));


        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                requestCaptor.capture(),
                any(ParameterizedTypeReference.class)
        );

        PlanningRequestDTO request = requestCaptor.getValue().getBody();

        boolean oldSlotBlocked = false;
        for (var block : request.getFixed_blocks()) {
            if (block.getStart() == 120) {
                oldSlotBlocked = true;
                break;
            }
        }
        assertTrue(oldSlotBlocked, "Der alte Slot 10:00 Uhr muss blockiert sein");


        verify(unitToMoveMock).setStartTime(any());

        verify(learningPlanRepository).save(existingPlanMock);

    }
}