package de.pse.oys.service.planning;


import de.pse.oys.domain.enums.AchievementLevel;
import de.pse.oys.domain.enums.ConcentrationLevel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.pse.oys.domain.CostMatrix;
import de.pse.oys.domain.LearningUnit;
import de.pse.oys.domain.Task;
import de.pse.oys.domain.UnitRating;
import de.pse.oys.dto.CostDTO;
import de.pse.oys.persistence.CostMatrixRepository;
import org.springframework.stereotype.Service;
import de.pse.oys.persistence.TaskRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import de.pse.oys.domain.Module;


@Service
public class LearningAnalyticsProvider {
    private final CostMatrixRepository costMatrixRepository;
    private final ObjectMapper objectMapper;
    private final TaskRepository taskRepository;


    public LearningAnalyticsProvider(CostMatrixRepository costMatrixRepository, ObjectMapper objectMapper,
                                     TaskRepository taskRepository) {
        this.costMatrixRepository = costMatrixRepository;
        this.objectMapper = objectMapper;
        this.taskRepository = taskRepository;
    }

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
        if (!costMatrix.isOutdated()){
            return makeCostDTOList(costMatrix.getCosts());
        }
        return calculateCostsFromRatings(task);


    }



    private List<CostDTO> makeCostDTOList(String jsonCosts) {
        if (jsonCosts == null || jsonCosts.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(jsonCosts, new TypeReference<List<CostDTO>>() {});
        } catch (Exception e) {

            System.err.println("Fehler beim Lesen der Kosten-Daten: " + e.getMessage());
            return Collections.emptyList();
        }

    }

    private List<CostDTO> calculateCostsFromRatings (Task task) {
        List<CostDTO> costs = new ArrayList<>();
        List<LearningUnit> units = task.getLearningUnits();

        if (units == null || units.isEmpty()) {
            return costs;
        }

        for (LearningUnit unit : units) {
            if (unit.isRated()) {
                UnitRating rating = unit.getRating();

                double concentration = mapConcentrationToValue(rating.getConcentration()) * 1.5;
                double achievement = mapAchievementToValue(rating.getAchievement());
                int totalCost = ((int) (concentration + achievement) * -1);

                LocalDateTime start = unit.getStartTime();
                if (start != null) {
                    int slot = (start.getHour() * 60 + start.getMinute()) / 5;
                    costs.add(new CostDTO(slot, totalCost));
                }
            }

        }
        persist(task, task.getCostMatrix(), costs);

        return costs;
    }

    private void persist (Task task, CostMatrix costMatrix, List<CostDTO> costs) {
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
        if (level == null) return 1; // Fallback

        switch (level) {
            case VERY_LOW:  return -2;
            case LOW:       return -1;
            case MEDIUM:    return 0;
            case HIGH:      return 1;
            case VERY_HIGH: return 2;
            default:        return 1;
        }
    }
    private int mapAchievementToValue(AchievementLevel level) {
        if (level == null) return 1; // Fallback

        switch (level) {
            case NONE:  return -2;
            case POOR:       return -1;
            case PARTIAL:    return 0;
            case GOOD:      return 1;
            case EXCELLENT: return 2;
            default:        return 1;
        }
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


}
