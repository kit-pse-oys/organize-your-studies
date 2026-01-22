package de.pse.oys.service;
import org.springframework.stereotype.Service;
import de.pse.oys.persistence.*;
import de.pse.oys.domain.*;

import java.util.List;
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
    public void PlanningService (UUID userId, LocalDate weekStart) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        List<Task> openTasks = taskRepository.findOpenTasksByUserId(userId);


    }
}
