package de.pse.oys.service.planning;

import de.pse.oys.domain.*;
import de.pse.oys.domain.enums.RecurrenceType;
import de.pse.oys.domain.enums.TaskStatus;
import de.pse.oys.domain.enums.TimeSlot;
import de.pse.oys.dto.CostDTO;
import de.pse.oys.dto.plan.FixedBlockDTO;
import de.pse.oys.dto.plan.PlanningRequestDTO;
import de.pse.oys.dto.plan.PlanningResponseDTO;
import de.pse.oys.dto.plan.PlanningTaskDTO;
import de.pse.oys.persistence.LearningPlanRepository;
import de.pse.oys.persistence.TaskRepository;
import de.pse.oys.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;


/**
 * PlanningService – Service-Klasse für die Planung eines Lernplans.
 *
 * @author uhxch
 * @version 1.0
 */
@Service
public class PlanningService {

    // --- MagicNumbers & Strings --- //
    private static final int SLOT_DURATION_MINUTES = 5;
    private static final int PLANNING_HORIZON_SLOTS = 2016;
    private static final int SLOTS_PER_DAY = 288;
    private static final int MINUTES_PER_DAY = 1440;
    private static final int DAYS_IN_WEEK_OFFSET = 6;
    private static final int RESCHEDULE_PENALTY_COST = 10;

    private static final String ID_SEPERATOR = "_";
    private static final String RESCHEDULE_SUFFIX = "_reschedule";


    private final TaskRepository taskRepository;
    private final LearningPlanRepository learningPlanRepository;
    private final UserRepository userRepository;
    private final LearningAnalyticsProvider learningAnalyticsProvider;
    private final RestTemplate restTemplate;


    @Value("${microservice.planning.url}")
    private String planningMicroserviceUrl;

    /**
     * Konstruktor für PlanningService.
     *
     * @param taskRepository
     * @param learningPlanRepository
     * @param userRepository
     * @param learningAnalyticsProvider
     * @param restTemplate
     */
    public PlanningService(TaskRepository taskRepository,
                           LearningPlanRepository learningPlanRepository,
                           UserRepository userRepository,
                           LearningAnalyticsProvider learningAnalyticsProvider,
                           RestTemplate restTemplate) {
        this.taskRepository = taskRepository;
        this.learningPlanRepository = learningPlanRepository;
        this.userRepository = userRepository;
        this.learningAnalyticsProvider = learningAnalyticsProvider;
        this.restTemplate = restTemplate;
    }


    /**
     * Kernfunktion. Lädt offene Tasks und Nutzer-Präferenzen sowie die aktuelle Kosten-
     * Matrix aus der Datenbank, berechnet den current_slot und transformiert diese in das
     * JSON-Format und sendet sie an den Python-Solver. Das Ergebnis wird als neuer Wochen-
     * plan gespeichert. Wirft eine EntityNotFoundException, falls der User nicht existiert.
     *
     * @param userId    Die ID des Benutzers.
     * @param weekStart Das Startdatum der Woche.
     * @throws IllegalArgumentException wenn der Benutzer nicht gefunden wird.
     */
    @Transactional
    public void generateWeeklyPlan(UUID userId, LocalDate weekStart) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        LocalDateTime now = LocalDateTime.now();
        int currentSlot = calculateCurrentSlot(weekStart, now);

        LearningPreferences userPreferences = user.getPreferences();
        List<Integer> blockedDays = calculateBlockedWeekDays(user, userPreferences);
        String preferredTimeSlots = mapPreferredTimeSlotsToString(userPreferences);
        List<FreeTime> freeTimes = user.getFreeTimes();
        List<FixedBlockDTO> fixedBlocksDTO = calculateFixedBlocksDTO(freeTimes, weekStart);
        List<PlanningTaskDTO> planningTaskDTOS = fetchOpenTasksAsDTOs(user, now, weekStart);

        PlanningRequestDTO planningInput = new PlanningRequestDTO(
                PLANNING_HORIZON_SLOTS,
                currentSlot,
                blockedDays,
                preferredTimeSlots,
                fixedBlocksDTO,
                planningTaskDTOS
        );
        List<PlanningResponseDTO> planningResults = callSolver(planningInput);

        if (planningResults != null && !planningResults.isEmpty()) {
            int breakDuration = userPreferences.getBreakDurationMinutes();
            saveLearningResults(planningResults, weekStart, breakDuration);
            System.out.println("Planungsergebnisse erfolgreich gespeichert.");
        } else {
            System.out.println("Keine Planungsergebnisse empfangen.");

        }
    }

    /**
     * Reschedult eine einzelne Lerneinheit innerhalb eines bestehenden Lernplans.
     *
     * @param userId             Die ID des Benutzers.
     * @param weekStart          Das Startdatum der Woche.
     * @param unitIdToReschedule Die ID der Lerneinheit, die neu terminiert werden soll.
     * @throws IllegalArgumentException wenn der Benutzer, Lernplan oder Lerneinheit nicht gefunden wird.
     */

    @Transactional
    public void rescheduleUnit(UUID userId, LocalDate weekStart, UUID unitIdToReschedule) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        LearningPlan plan = learningPlanRepository.findByUserIdAndWeekStart(unitIdToReschedule, weekStart).orElse(null);
        if (plan == null) {
            throw new IllegalArgumentException("Learning Plan not found for the specified week");
        }
        List<LearningUnit> units = plan.getUnits();
        LearningUnit unitToReschedule = null;
        for (LearningUnit unit : units) {
            if (unit.getUnitId().equals(unitIdToReschedule)) {
                unitToReschedule = unit;
                break;
            }
        }
        if (unitToReschedule == null) {
            throw new IllegalArgumentException("Learning Unit not found in the specified plan");
        }
        List<FixedBlockDTO> fixedBlocksDTO = createFixedBlocksFromExistingPlan(units, weekStart);
        Task parentTask = unitToReschedule.getTask();
        applyPenaltyToCostMatrix(parentTask, unitToReschedule, weekStart);

        int unitDurationMinutes = (int) ChronoUnit.MINUTES.between(unitToReschedule.getStartTime(), unitToReschedule.getEndTime());
        int durationSlots = (int) Math.ceil(unitDurationMinutes / (double) SLOT_DURATION_MINUTES);
        String chunkId = parentTask.getTaskId().toString() + RESCHEDULE_SUFFIX;
        LocalDateTime softDeadline = parentTask.getSoftDeadline(user.getPreferences().getDeadlineBufferDays());
        int deadlineSlot = mapLocalDateTimeToSlot(softDeadline, weekStart);
        List<CostDTO> costs = learningAnalyticsProvider.getCostMatrixForTask(parentTask);
        PlanningTaskDTO planningTaskDTO = new PlanningTaskDTO(chunkId, durationSlots, deadlineSlot, costs);
        List<PlanningTaskDTO> planningTaskDTOS = new ArrayList<>();
        planningTaskDTOS.add(planningTaskDTO);
        LocalDateTime now = LocalDateTime.now();
        int horizon = PLANNING_HORIZON_SLOTS;
        int currentSlot = calculateCurrentSlot(weekStart, now);

        PlanningRequestDTO planningInput = new PlanningRequestDTO(
                horizon,
                currentSlot,
                calculateBlockedWeekDays(user, user.getPreferences()),
                mapPreferredTimeSlotsToString(user.getPreferences()),
                fixedBlocksDTO,
                planningTaskDTOS
        );
        List<PlanningResponseDTO> planningResults = callSolver(planningInput);
        if (planningResults != null && !planningResults.isEmpty()) {

            PlanningResponseDTO planningResult = planningResults.get(0);

            LocalDateTime newStart = mapSlotToDateTime(planningResult.getStart(), weekStart);
            LocalDateTime newEnd = mapSlotToDateTime(planningResult.getEnd(), weekStart);

            int breakDuration = user.getPreferences().getBreakDurationMinutes();
            if (java.time.Duration.between(newStart, newEnd).toMinutes() > breakDuration) {
                newEnd = newEnd.minusMinutes(breakDuration);
            }
            unitToReschedule.setStartTime(newStart);
            unitToReschedule.setEndTime(newEnd);
            learningPlanRepository.save(plan);


        } else {
            System.out.println("Keine Planungsergebnisse für Rescheduling empfangen.");
        }


    }

    private void applyPenaltyToCostMatrix(Task task, LearningUnit unit, LocalDate weekStart) {
        LocalDateTime startTime = unit.getStartTime();
        int penaltySlot = mapLocalDateTimeToSlot(startTime, weekStart);
        int penaltyCost = RESCHEDULE_PENALTY_COST;
        learningAnalyticsProvider.applyPenaltyToCostMatrix(task, penaltySlot, penaltyCost);
    }

    /**
     * Erstellt feste Blöcke aus einem bestehenden Lernplan, um sie dem Solver zu übergeben.
     *
     * @param freeTimes Die Liste der Lerneinheiten, die als feste Blöcke betrachtet werden sollen.
     * @param weekStart Das Startdatum der Woche.
     * @return Liste der FreetimeDTOs, die die festen Blöcke repräsentieren.
     */

    private List<FixedBlockDTO> createFixedBlocksFromExistingPlan(List<LearningUnit> freeTimes, LocalDate weekStart) {
        List<FixedBlockDTO> fixedBlocksDTO = new ArrayList<>();


        for (LearningUnit unit : freeTimes) {
            LocalDateTime unitStart = unit.getStartTime();
            LocalDateTime unitEnd = unit.getEndTime();

            int startSlot = mapLocalDateTimeToSlot(unitStart, weekStart);
            long durationMinutes = Duration.between(unitStart, unitEnd).toMinutes();
            int durationSlots = (int) (durationMinutes / SLOT_DURATION_MINUTES);

            if (startSlot >= 0 && durationSlots > 0) {
                fixedBlocksDTO.add(new FixedBlockDTO(startSlot, durationSlots));
            }

        }
        return fixedBlocksDTO;
    }

    /**
     * Sendet die Planungseingabedaten an den Python-Solver und empfängt die Planungsergebnisse.
     *
     * @param requestDTO Die Planungseingabedaten.
     * @return Liste der Planungsergebnisse vom Solver.
     */

    private List<PlanningResponseDTO> callSolver(PlanningRequestDTO requestDTO) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PlanningRequestDTO> requestEntity = new HttpEntity<>(requestDTO, headers);

        try {
            ResponseEntity<List<PlanningResponseDTO>> responseEntity = restTemplate.exchange(
                    planningMicroserviceUrl,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<List<PlanningResponseDTO>>() {
                    }
            );
            return responseEntity.getBody();
        } catch (Exception e) {
            System.err.println("Fehler bei der Kommunikation mit dem Solver: " + e.getMessage());
            return Collections.emptyList();
        }
    }


    /**
     * Speichert die Planungsergebnisse als neue Lerneinheiten und verknüpft sie mit den
     * entsprechenden Aufgaben. Erstellt einen neuen LearningPlan für die Woche.
     *
     * @param results   Die Liste der Planungsergebnisse vom Solver.
     * @param weekStart Das Startdatum der Woche.
     */
    private void saveLearningResults(List<PlanningResponseDTO> results, LocalDate weekStart, int breakDuration) {
        LearningPlan plan = new LearningPlan(weekStart, weekStart.plusDays(DAYS_IN_WEEK_OFFSET));
        List<LearningUnit> newLearningUnits = new ArrayList<>();
        for (PlanningResponseDTO result : results) {
            String id = result.getId();
            String originalTaskIdStr = id.split(ID_SEPERATOR)[0];
            UUID originalTaskId = UUID.fromString(originalTaskIdStr);

            Task task = taskRepository.findById(originalTaskId).orElse(null);
            if (task != null) {
                LocalDateTime startDateTime = mapSlotToDateTime(result.getStart(), weekStart);
                LocalDateTime endDateTime = mapSlotToDateTime(result.getEnd(), weekStart);
                if (java.time.Duration.between(startDateTime, endDateTime).toMinutes() > breakDuration) {
                    endDateTime = endDateTime.minusMinutes(breakDuration);
                }
                LearningUnit unit = new LearningUnit(task, startDateTime, endDateTime);
                newLearningUnits.add(unit);
                task.addLearningUnit(unit);
                taskRepository.save(task);
            }
        }
        plan.setUnits(newLearningUnits);
        learningPlanRepository.save(plan);
    }

    /**
     * Mappt einen Slot auf ein LocalDateTime-Objekt basierend auf dem Wochenstartdatum.
     *
     * @param slot      Der zu mappende Slot.
     * @param weekStart Das Startdatum der Woche.
     * @return Das entsprechende LocalDateTime-Objekt.
     */
    private LocalDateTime mapSlotToDateTime(int slot, LocalDate weekStart) {
        int totalMinutes = slot * SLOT_DURATION_MINUTES;
        int daysToAdd = totalMinutes / MINUTES_PER_DAY;
        int minutesInDay = totalMinutes % MINUTES_PER_DAY;
        int hours = minutesInDay / 60;
        int minutes = minutesInDay % 60;

        LocalDate targetDate = weekStart.plusDays(daysToAdd);
        return LocalDateTime.of(targetDate, LocalTime.of(hours, minutes));
    }

    /**
     * Lädt alle offenen Aufgaben eines Nutzers, berechnet die verbleibende Dauer
     * und teilt sie in Lerneinheiten auf, die als TaskDTOs zurückgegeben werden.
     *
     * @param user      Der Nutzer, dessen Aufgaben geladen werden sollen.
     * @param now       Das aktuelle Datum und Uhrzeit.
     * @param weekStart Das Startdatum der Woche.
     * @return Liste der TaskDTOs für offene Aufgaben.
     */
    private List<PlanningTaskDTO> fetchOpenTasksAsDTOs(User user, LocalDateTime now, LocalDate weekStart) {
        List<Task> openTasks = taskRepository.findAllByModuleUserUserIdAndStatus(user.getId(), TaskStatus.OPEN);
        List<PlanningTaskDTO> planningTaskDTOS = new ArrayList<>();
        LearningPreferences userPreferences = user.getPreferences();
        LocalDate endOfWeek = weekStart.plusDays(DAYS_IN_WEEK_OFFSET);

        for (Task task : openTasks) {
            int durationExistingUnits = 0;
            List<LearningUnit> existingUnits = task.getLearningUnits();
            if (existingUnits != null) {
                for (LearningUnit unit : existingUnits) {
                    LocalDate unitDate = unit.getStartTime().toLocalDate();
                    LocalDateTime unitDateTime = unit.getStartTime();
                    if (unitDate == null) {
                        continue;
                    }

                    boolean isInCurrentWeek = !unitDate.isBefore(weekStart) && !unitDate.isAfter(endOfWeek);
                    boolean isInPast = unitDateTime.isBefore(now);
                    if (isInCurrentWeek && isInPast) {
                        long minutes = ChronoUnit.MINUTES.between(unit.getStartTime(), unit.getEndTime());
                        durationExistingUnits += (int) minutes;
                    }


                }
            }

            int restDuration = task.getWeeklyDurationMinutes() - durationExistingUnits;


            if (restDuration <= 0) {
                continue;
            }

            int targetUnitDuration = calculateTargetUnitDuration(userPreferences, task);


            List<PlanningTaskDTO> unitChunks = splitIntoChunks(task, restDuration,
                    targetUnitDuration, userPreferences.getBreakDurationMinutes(),
                    userPreferences.getDeadlineBufferDays(), weekStart);
            planningTaskDTOS.addAll(unitChunks);

        }

        return planningTaskDTOS;
    }

    /**
     * Teilt eine Aufgabe in mehrere Chunks auf, basierend auf der verbleibenden Dauer und
     * der Ziel-Dauer pro Lerneinheit.
     *
     * @param task               Die Aufgabe, die aufgeteilt werden soll.
     * @param restDuration       Die verbleibende Dauer der Aufgabe in Minuten.
     * @param targetUnitDuration Die Ziel-Dauer pro Lerneinheit in Minuten.
     * @param breakDuration      Die Pausendauer zwischen den Lerneinheiten in Minuten.
     * @param bufferDays         Die Pufferzeit vor Deadlines in Tagen.
     * @param weekStart          Das Startdatum der Woche.
     * @return Liste der aufgeteilten TaskDTOs.
     */
    private List<PlanningTaskDTO> splitIntoChunks(Task task, int restDuration, int targetUnitDuration, int breakDuration, int bufferDays, LocalDate weekStart) {
        List<PlanningTaskDTO> chunks = new ArrayList<>();

        long n = Math.round((double) restDuration / targetUnitDuration);

        if (n == 0) {
            n = 1;
        }
        int baseChunkDuration = restDuration / (int) n;
        int remainder = restDuration % (int) n;
        List<CostDTO> costs = learningAnalyticsProvider.getCostMatrixForTask(task);
        for (int i = 0; i < n; i++) {
            int duration = baseChunkDuration;
            if (i < remainder) {
                duration += 1; // Verteile den Rest auf die ersten Chunks
            }
            int chunkDurationWithBreakPadding = duration + breakDuration;
            int durationSlots = (int) Math.ceil(chunkDurationWithBreakPadding / (double) SLOT_DURATION_MINUTES);

            String chunkId = task.getTaskId().toString() + ID_SEPERATOR + i;

            LocalDateTime softDeadline = task.getSoftDeadline(bufferDays);
            int deadlineSlot = mapLocalDateTimeToSlot(softDeadline, weekStart);

            PlanningTaskDTO dto = new PlanningTaskDTO(chunkId, durationSlots, deadlineSlot, costs);
            chunks.add(dto);
        }
        return chunks;

    }

    /*** Berechnet die Ziel-Dauer für Lerneinheiten basierend auf Nutzerpräferenzen und Feedback.
     *
     * @param prefs Die Lernpräferenzen des Nutzers.
     * @param task  Die Aufgabe, für die die Ziel-Dauer berechnet werden soll.
     * @return Die berechnete Ziel-Dauer in Minuten.
     */
    private int calculateTargetUnitDuration(LearningPreferences prefs, Task task) {
        int dMin = prefs.getMinUnitDurationMinutes();
        int dMax = prefs.getMaxUnitDurationMinutes();

        double feedbackFactor = calculateFeedbackFactor(task);
        int baseDuration = (dMin + dMax) / 2;
        return (int) Math.max(dMin, Math.min(baseDuration * (1 + feedbackFactor), dMax));
    }

    /**
     * Berechnet den Feedback-Faktor basierend auf den Bewertungen der Lerneinheiten einer Aufgabe.
     *
     * @param task Die Aufgabe, für die der Feedback-Faktor berechnet werden soll.
     * @return Der berechnete Feedback-Faktor als double-Wert.
     */
    private double calculateFeedbackFactor(Task task) {
        List<LearningUnit> units = task.getLearningUnits();
        if (units == null || units.isEmpty()) {
            return 0.0; // Kein Feedback, Standardfaktor
        }
        double totalRating = 0.0;
        int ratedUnitsCount = 0;

        for (LearningUnit unit : units) {
            if (unit.isRated() && unit.getRating() != null && unit.getRating().getPerceivedDuration() != null) {
                ratedUnitsCount++;
                totalRating += unit.getRating().getPerceivedDuration().getAdjustmentValue();
            }
        }
        if (ratedUnitsCount == 0) {
            return 0.0; // Kein Feedback, Standardfaktor
        }
        return totalRating / ratedUnitsCount;


    }

    /**
     * Wandelt die FreeTime-Objekte (Entities) in DTOs um, die Python versteht.
     * Filtert Termine raus, die nicht in die aktuelle Woche fallen.
     */
    private List<FixedBlockDTO> calculateFixedBlocksDTO(List<FreeTime> freeTimes, LocalDate weekStart) {
        List<FixedBlockDTO> dtos = new ArrayList<>();

        LocalDate weekEnd = weekStart.plusDays(DAYS_IN_WEEK_OFFSET);

        for (FreeTime freeTime : freeTimes) {
            Integer dayIndex = null;
            RecurrenceType type = freeTime.getRecurrenceType();
            if (type == RecurrenceType.WEEKLY) {
                RecurringFreeTime weekly = (RecurringFreeTime) freeTime;
                dayIndex = weekly.getDayOfWeek().getValue() - 1;
            } else if (type == RecurrenceType.ONCE) {
                SingleFreeTime single = (SingleFreeTime) freeTime;
                LocalDate date = single.getDate();
                if (!date.isBefore(weekStart) && !date.isAfter(weekEnd)) {
                    dayIndex = (int) ChronoUnit.DAYS.between(weekStart, date);
                }
            }
            if (dayIndex != null) {
                int dayOffset = dayIndex * SLOTS_PER_DAY;

                int timeSlot = mapTimeToSlot(freeTime.getStartTime());
                int absoluteStart = dayOffset + timeSlot;
                long durationMinutes = Duration.between(freeTime.getStartTime(), freeTime.getEndTime()).toMinutes();
                int durationSlots = (int) (durationMinutes / SLOT_DURATION_MINUTES);
                dtos.add(new FixedBlockDTO(absoluteStart, durationSlots));
            }
        }
        return dtos;
    }

    /**
     * Mappt eine LocalTime-Objekt auf einen Slot.
     *
     * @param time Das zu mappende LocalTime-Objekt.
     * @return Der entsprechende Slot als Integer-Wert.
     */

    private int mapTimeToSlot(LocalTime time) {
        int totalMinutes = time.getHour() * 60 + time.getMinute();
        return totalMinutes / SLOT_DURATION_MINUTES;
    }

    /**
     * Mappt ein LocalDateTime-Objekt auf einen Slot basierend auf dem Wochenstartdatum.
     *
     * @param targetTime Das zu mappende LocalDateTime-Objekt.
     * @param weekStart  Das Startdatum der Woche.
     * @return Der entsprechende Slot als Integer-Wert.
     */
    private int mapLocalDateTimeToSlot(LocalDateTime targetTime, LocalDate weekStart) {

        LocalDateTime startAnchor = LocalDateTime.of(weekStart, LocalTime.MIN);


        long minutesBetween = ChronoUnit.MINUTES.between(startAnchor, targetTime);

        // 3. In Slots umrechnen
        return (int) (minutesBetween / SLOT_DURATION_MINUTES);
    }

    /**
     * Berechnet die blockierten Wochentage basierend auf den Nutzerpräferenzen.
     *
     * @param user            Der Nutzer.
     * @param userPreferences Die Lernpräferenzen des Nutzers.
     * @return Liste der blockierten Wochentage als Integer-Werte (0=Montag, 6=Sonntag).
     */

    private List<Integer> calculateBlockedWeekDays(User user, LearningPreferences userPreferences) {
        List<Integer> blockedDays = new ArrayList<>();
        Set<DayOfWeek> preferredDays = userPreferences.getPreferredDays();

        for (DayOfWeek day : DayOfWeek.values()) {
            if (!preferredDays.contains(day)) {
                blockedDays.add(day.getValue() - 1);
            }
            ;
        }

        return blockedDays;

    }

    /**
     * Berechnet den aktuellen Slot basierend auf dem Wochenstartdatum.
     *
     * @param weekStart Das Startdatum der Woche.
     * @return Der aktuelle Slot als Integer-Wert.
     */
    private int calculateCurrentSlot(LocalDate weekStart, LocalDateTime now) {

        LocalDateTime weekStartDateTime = LocalDateTime.of(weekStart, LocalTime.of(0, 0));

        long minutesBetween = ChronoUnit.MINUTES.between(weekStartDateTime, now);
        int currentSlot = (int) (minutesBetween / SLOT_DURATION_MINUTES);
        return currentSlot;
    }

    /**
     * Mappt die bevorzugten Zeitslots des Nutzers in eine kommagetrennte String-Darstellung.
     *
     * @param preferences Die Lernpräferenzen des Nutzers.
     * @return Kommagetrennter String der bevorzugten Zeitslots.
     */

    private String mapPreferredTimeSlotsToString(LearningPreferences preferences) {
        Set<TimeSlot> preferredSlots = preferences.getPreferredTimeSlots();
        List<String> slotStrings = new ArrayList<>();

        for (TimeSlot slot : preferredSlots) {
            slotStrings.add(slot.name());
        }
        return String.join(",", slotStrings);
    }

}
