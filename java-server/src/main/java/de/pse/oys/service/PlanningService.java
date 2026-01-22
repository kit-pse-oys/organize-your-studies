package de.pse.oys.service;
import de.pse.oys.domain.enums.TimeSlot;
import de.pse.oys.dto.FreetimeDTO;
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
        List<Task> openTasks = taskRepository.findOpenTasksByUserId(userId);

        int currentSlot = calculateCurrentSlot(weekStart);

        LearningPreferences userPreferences = user.getPreferences();
        List<Integer> blockedDays = calculateBlockedWeekDays(user, userPreferences);
        String preferredTimeSlots = mapPreferredTimeSlotsToString(userPreferences);
        List<FreeTime> freeTimes = user.getFreeTimes();
        List<FreetimeDTO> fixedBlocksDTO = calculateFixedBlocksDTO(freeTimes);







    }

    private List<FreetimeDTO> calculateFixedBlocksDTO(List<FreeTime> freeTimes) {
        List<FreetimeDTO> dtos = new ArrayList<>();
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

    private String mapPreferredTimeSlotsToString(LearningPreferences preferences) {
        List<TimeSlot> preferredSlots = preferences.getPreferredTimeSlots();
        List<String> slotStrings = new ArrayList<>();
        for (TimeSlot slot : preferredSlots) {
            switch (slot.name()) {
                case "MORNING":     slotStrings.add("morgens"); break;
                case "FORENOON":    slotStrings.add("vormittags"); break;
                case "NOON":        slotStrings.add("mittags"); break;
                case "AFTERNOON":   slotStrings.add("nachmittags"); break;
                case "EVENING":     slotStrings.add("abends"); break;
                default: break;
            }
        }

        return String.join(",", slotStrings);
    }

}
