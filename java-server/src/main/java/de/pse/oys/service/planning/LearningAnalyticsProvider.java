package de.pse.oys.service.planning;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.pse.oys.domain.*;
import de.pse.oys.domain.Module;
import de.pse.oys.domain.enums.AchievementLevel;
import de.pse.oys.domain.enums.ConcentrationLevel;
import de.pse.oys.dto.CostDTO;
import de.pse.oys.persistence.CostMatrixRepository;
import de.pse.oys.persistence.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


/**
 * LearningAnalyticsProvider – Service zur Berechnung und Verwaltung von Kostenmatrizen für Lernaufgaben.
 * Kernfunktionen:
 * - Kostenberechnung: Berechnet Kosten basierend auf Konzentrations- und Leistungsbewertungen der Lerneinheiten.
 * - Heuristische Kostenableitung: Leitet Kosten von ähnlichen Aufgaben im selben Modul ab, wenn keine aktuellen Bewertungen vorliegen.
 * - Kostenaktualisierung: Ermöglicht die Aktualisierung der Kostenmatrix mit Strafkosten für bestimmte Zeit-Slots.
 * @author uhxch
 */
@Service
public class LearningAnalyticsProvider {
    private static final double CONCENTRATION_WEIGHT = 1.5;
    private static final int COST_INVERSION_FACTOR = -1;

    private static final int MINUTES_PER_HOUR = 60;
    private static final int SLOT_DURATION_MINUTES = 5;

    private static final int VAL_CONCENTRATION_VERY_LOW = -2;
    private static final int VAL_CONCENTRATION_LOW = -1;
    private static final int VAL_CONCENTRATION_MEDIUM = 0;
    private static final int VAL_CONCENTRATION_HIGH = 1;
    private static final int VAL_CONCENTRATION_VERY_HIGH = 2;
    private static final int VAL_CONCENTRATION_DEFAULT = 1;

    private static final int VAL_ACHIEVEMENT_NONE = -2;
    private static final int VAL_ACHIEVEMENT_POOR = -1;
    private static final int VAL_ACHIEVEMENT_PARTIAL = 0;
    private static final int VAL_ACHIEVEMENT_GOOD = 1;
    private static final int VAL_ACHIEVEMENT_EXCELLENT = 2;
    private static final int VAL_ACHIEVEMENT_DEFAULT = 1;

    private static final Map<ConcentrationLevel, Integer> CONCENTRATION_MAP = Map.ofEntries(
            Map.entry(ConcentrationLevel.VERY_LOW, VAL_CONCENTRATION_VERY_LOW),
            Map.entry(ConcentrationLevel.LOW, VAL_CONCENTRATION_LOW),
            Map.entry(ConcentrationLevel.MEDIUM, VAL_CONCENTRATION_MEDIUM),
            Map.entry(ConcentrationLevel.HIGH, VAL_CONCENTRATION_HIGH),
            Map.entry(ConcentrationLevel.VERY_HIGH, VAL_CONCENTRATION_VERY_HIGH)
    );

    private static final Map<AchievementLevel, Integer> ACHIEVEMENT_MAP = Map.ofEntries(
            Map.entry(AchievementLevel.NONE, VAL_ACHIEVEMENT_NONE),
            Map.entry(AchievementLevel.POOR, VAL_ACHIEVEMENT_POOR),
            Map.entry(AchievementLevel.PARTIAL, VAL_ACHIEVEMENT_PARTIAL),
            Map.entry(AchievementLevel.GOOD, VAL_ACHIEVEMENT_GOOD),
            Map.entry(AchievementLevel.EXCELLENT, VAL_ACHIEVEMENT_EXCELLENT)
    );

    private final CostMatrixRepository costMatrixRepository;
    private final ObjectMapper objectMapper;
    private final TaskRepository taskRepository;


    /**
     * Konstruktor mit Dependency Injection.
     * @param costMatrixRepository das Repository für den Zugriff auf CostMatrix-Entitäten in der Datenbank.
     * @param objectMapper die Jackson ObjectMapper-Instanz für die JSON-Verarbeitung von Kosten-Daten.
     * @param taskRepository das Repository für den Zugriff auf Task-Entitäten, benötigt für die Persistierung von Änderungen.
     */
    public LearningAnalyticsProvider(CostMatrixRepository costMatrixRepository, ObjectMapper objectMapper,
                                     TaskRepository taskRepository) {
        this.costMatrixRepository = costMatrixRepository;
        this.objectMapper = objectMapper;
        this.taskRepository = taskRepository;
    }

    /**
     * Gibt die Kostenmatrix für eine gegebene Task zurück.
     * @param task Die Task, für die die Kostenmatrix abgerufen werden soll. Darf nicht null sein.
     * @return Eine Liste von CostDTOs, die die Kosten für die Task repräsentieren.
     */
    public List<CostDTO> getCostMatrixForTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Error: Task darf nicht null sein");
        }
        CostMatrix costMatrix = task.getCostMatrix();

        if (costMatrix == null) {

            List<CostDTO> fromRatings = calculateCostsFromRatings(task);
            if (!fromRatings.isEmpty()) {
                return fromRatings;
            }
            return calculateHeuristiksFromTask(task);
        }
        if (!costMatrix.isOutdated()) {
            return makeCostDTOList(costMatrix.getCosts());
        }
        return calculateCostsFromRatings(task);


    }


    private List<CostDTO> makeCostDTOList(String jsonCosts) {
        if (jsonCosts == null || jsonCosts.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(jsonCosts, new TypeReference<List<CostDTO>>() {
            });
        } catch (Exception e) {

            System.err.println("Fehler beim Lesen der Kosten-Daten: " + e.getMessage());
            return Collections.emptyList();
        }

    }

    private List<CostDTO> calculateCostsFromRatings(Task task) {
        List<CostDTO> costs = new ArrayList<>();
        List<LearningUnit> units = task.getLearningUnits();

        if (units == null || units.isEmpty()) {
            return costs;
        }

        for (LearningUnit unit : units) {
            if (unit.isRated()) {
                UnitRating rating = unit.getRating();

                double concentration = mapConcentrationToValue(rating.getConcentration()) * CONCENTRATION_WEIGHT;
                double achievement = mapAchievementToValue(rating.getAchievement());
                int totalCost = ((int) (concentration + achievement) * COST_INVERSION_FACTOR);

                LocalDateTime start = unit.getStartTime();
                if (start != null) {
                    int slot = (start.getHour() * MINUTES_PER_HOUR + start.getMinute()) / SLOT_DURATION_MINUTES;
                    costs.add(new CostDTO(slot, totalCost));
                }
            }

        }
        persist(task.getCostMatrix(), costs);

        return costs;
    }

    private void persist(CostMatrix costMatrix, List<CostDTO> costs) {
        if (costMatrix == null) {
            return;
        }

        try {
            String jsonCosts = objectMapper.writeValueAsString(costs);
            costMatrix.setCosts(jsonCosts);
            costMatrixRepository.save(costMatrix);
        } catch (Exception e) {
            System.err.println("Fehler beim Speichern der Kosten-Daten: " + e.getMessage());
        }


    }


    private int mapConcentrationToValue(ConcentrationLevel level) {
        if (level == null) {
            return VAL_CONCENTRATION_DEFAULT;
        }
        return CONCENTRATION_MAP.getOrDefault(level, VAL_CONCENTRATION_DEFAULT);
    }

    private int mapAchievementToValue(AchievementLevel level) {
        if (level == null) {
            return VAL_ACHIEVEMENT_DEFAULT;
        }
        return ACHIEVEMENT_MAP.getOrDefault(level, VAL_ACHIEVEMENT_DEFAULT);
    }

    private List<CostDTO> calculateHeuristiksFromTask(Task currentTask) {
        Module module = currentTask.getModule();
        if (module == null) {
            return Collections.emptyList();
        }

        List<Task> tasks = module.getTasks();

        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyList();
        }

        for (Task task : tasks) {
            boolean isDifferentTask = !task.getTaskId().equals(currentTask.getTaskId());
            boolean isSameCategory = task.getCategory() == currentTask.getCategory();
            boolean hasValidData = task.getCostMatrix() != null && task.getCostMatrix().getCosts() != null;
            if (isDifferentTask && isSameCategory && hasValidData) {
                return makeCostDTOList(task.getCostMatrix().getCosts());
            }
        }

        return Collections.emptyList();


    }


    /**
     * Fügt der Kostenmatrix einer Task Strafkosten hinzu oder aktualisiert sie, wenn bereits Kosten für den Slot existieren.
     * @param task Die Task, deren Kostenmatrix aktualisiert werden soll. Darf nicht null sein.
     * @param penaltySlot Der Zeit-Slot welcher bestraft werden soll.
     * @param penaltyCost Die Höhe der Strafkosten, die zum bestehenden Wert addiert werden sollen. Kann positiv oder negativ sein.
     */
    public void applyPenaltyToCostMatrix(Task task, int penaltySlot, int penaltyCost) {
        if (task == null) {
            throw new IllegalArgumentException("Error: Task darf nicht null sein");
        }
        CostMatrix costMatrix = task.getCostMatrix();
        List<CostDTO> currentCosts;
        boolean newMatrix = false;
        if (costMatrix == null) {
            currentCosts = new ArrayList<>();
            costMatrix = new CostMatrix("[]", task);
            task.setCostMatrix(costMatrix);
            newMatrix = true;
        } else {
            currentCosts = makeCostDTOList(costMatrix.getCosts());
        }

        Optional<CostDTO> existingCostOpt = currentCosts.stream()
                .filter(c -> c.getT() == penaltySlot)
                .findFirst();
        if (existingCostOpt.isPresent()) {
            CostDTO existingCost = existingCostOpt.get();
            existingCost.setC(existingCost.getC() + penaltyCost);
        } else {
            currentCosts.add(new CostDTO(penaltySlot, penaltyCost));
        }

        currentCosts.sort(Comparator.comparingInt(CostDTO::getT));
        try {
            String jsonCosts = objectMapper.writeValueAsString(currentCosts);
            costMatrix.setCosts(jsonCosts);
            costMatrixRepository.save(costMatrix);

            if (newMatrix) {
                taskRepository.save(task);
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Speichern der Kosten-Daten oder der Task: " + e.getMessage());
        }

    }
}
