package de.pse.oys.service.planning;
import de.pse.oys.domain.enums.RecurrenceType;
import de.pse.oys.domain.enums.TimeSlot;
import de.pse.oys.dto.FreetimeDTO;
import de.pse.oys.dto.TaskDTO;
import org.springframework.stereotype.Service;
import de.pse.oys.persistence.*;
import de.pse.oys.domain.*;


import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDate;
/**
 * PlanningService – TODO: Beschreibung ergänzen
 *
 * @author uhxch
 * @version 1.0
 */
@Service
public class PlanningService {
       private final TaskRepository taskRepository;
       private final LearningPlanRepository learningPlanRepository;
       private final UserRepository userRepository;
       private final CostMatrixRepository costMatrixRepository;
       private final LearningAnalyticsProvider learningAnalyticsProvider;

    public PlanningService(TaskRepository taskRepository, LearningPlanRepository learningPlanRepository, UserRepository userRepository, CostMatrixRepository costMatrixRepository, LearningAnalyticsProvider learningAnalyticsProvider) {
        this.taskRepository = taskRepository;
        this.learningPlanRepository = learningPlanRepository;
        this.userRepository = userRepository;
        this.costMatrixRepository = costMatrixRepository;
        this.learningAnalyticsProvider = learningAnalyticsProvider;
    }


    /** Kernfunktion. Lädt offene Tasks und Nutzer-Präferenzen sowie die aktuelle Kosten-
        Matrix aus der Datenbank, berechnet den current_slot und transformiert diese in das
        JSON-Format und sendet sie an den Python-Solver. Das Ergebnis wird als neuer Wochen-
        plan gespeichert. Wirft eine EntityNotFoundException, falls der User nicht existiert.
     *
     * @param userId    Die ID des Benutzers.
     * @param weekStart Das Startdatum der Woche.
     * @throws IllegalArgumentException wenn der Benutzer nicht gefunden wird.
     */
    public void generateWeeklyPlan (UUID userId, LocalDate weekStart) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        int horizon = 2016;
        int currentSlot = calculateCurrentSlot(weekStart);

        LearningPreferences userPreferences = user.getPreferences();
        List<Integer> blockedDays = calculateBlockedWeekDays(user, userPreferences);
        String preferredTimeSlots = mapPreferredTimeSlotsToString(userPreferences);
        List<FreeTime> freeTimes = user.getFreeTimes();
        List<FreetimeDTO> fixedBlocksDTO = calculateFixedBlocksDTO(freeTimes, weekStart);
        List<TaskDTO> taskDTOs = new ArrayList<>();


    }



    /**
     * Wandelt die FreeTime-Objekte (Entities) in DTOs um, die Python versteht.
     * Filtert Termine raus, die nicht in die aktuelle Woche fallen.
     */
    private List<FreetimeDTO> calculateFixedBlocksDTO(List<FreeTime> freeTimes, LocalDate weekStart) {
        List<FreetimeDTO> dtos = new ArrayList<>();

        LocalDate weekEnd = weekStart.plusDays(6);

        for (FreeTime freeTime : freeTimes) {
            Integer dayIndex = null;
            RecurrenceType type = freeTime.getRecurrenceType();
            if (type == RecurrenceType.WEEKLY) {
                RecurringFreeTime weekly = (RecurringFreeTime) freeTime;
                dayIndex = weekly.getDayOfWeek().getValue() - 1;
            }
            else if (type == RecurrenceType.ONCE) { // oder SINGLE, je nach Enum-Name
                    SingleFreeTime single = (SingleFreeTime) freeTime;
                    LocalDate date = single.getDate();
                    if (!date.isBefore(weekStart) && !date.isAfter(weekEnd)) {
                        dayIndex = (int) ChronoUnit.DAYS.between(weekStart, date);
                    }
            }
            if (dayIndex != null) {
                int dayOffset = dayIndex * 288;

                int timeSlot = mapTimeToSlot(freeTime.getStartTime());
                int absoluteStart = dayOffset + timeSlot;

                long durationMinutes = java.time.Duration.between(freeTime.getStartTime(), freeTime.getEndTime()).toMinutes();
                int durationSlots = (int) (durationMinutes / 5);
                dtos.add(new FreetimeDTO(absoluteStart, durationSlots));
            }
        }
        return dtos;
    }


    private int mapTimeToSlot(LocalTime time) {
        int totalMinutes = time.getHour() * 60 + time.getMinute();
        return totalMinutes / 5;
    }
    /** Berechnet die blockierten Wochentage basierend auf den Nutzerpräferenzen.
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
                blockedDays.add(day.getValue() -1);
            };
        }

        return blockedDays;

    }
    /** Berechnet den aktuellen Slot basierend auf dem Wochenstartdatum.
     *
     * @param weekStart Das Startdatum der Woche.
     * @return Der aktuelle Slot als Integer-Wert.
     */

    private int calculateCurrentSlot(LocalDate weekStart) {
        // Slot 0 ist Montag, 00:00 Uhr
        LocalDateTime weekStartDateTime = LocalDateTime.of(weekStart, LocalTime.of(0, 0));
        LocalDateTime now = LocalDateTime.now();

        long minutesBetween = ChronoUnit.MINUTES.between(weekStartDateTime, now);
        int currentSlot = (int) (minutesBetween / 5);
        return currentSlot;
    }
    /** Mappt die bevorzugten Zeitslots des Nutzers in eine kommagetrennte String-Darstellung.
     *
     * @param preferences Die Lernpräferenzen des Nutzers.
     * @return Kommagetrennter String der bevorzugten Zeitslots.
     */

    private String mapPreferredTimeSlotsToString(LearningPreferences preferences) {
        Set<TimeSlot> preferredSlots = preferences.getPreferredTimeSlots();
        List<String> slotStrings = new ArrayList<>();

        for (TimeSlot slot : preferredSlots) {
            switch (slot) {
                case MORNING:
                    slotStrings.add("morgens");
                    break;
                case FORENOON:
                    slotStrings.add("vormittags");
                    break;
                case NOON:
                    slotStrings.add("mittags");
                    break;
                case AFTERNOON:
                    slotStrings.add("nachmittags");
                    break;
                case EVENING:
                    slotStrings.add("abends");
                    break;
                default:
                    break;
            }
        }
        return String.join(",", slotStrings);
    }

}
