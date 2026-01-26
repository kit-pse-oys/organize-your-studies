package de.pse.oys.dto;
import java.util.List;
import java.util.UUID;
public class PlanningRequest {
    private int horizon;
    private int current_slot;
    private List<Integer> blocked_days;
    private  String preference_time;
    private List<FreetimeDTO> fixed_blocks;
    private List<TaskDTO> tasks;

    public PlanningRequest(int horizon, int current_slot, List<Integer> blocked_days, String preference_time,
                           List<FreetimeDTO> fixed_blocks, List<TaskDTO> tasks) {
        this.horizon = horizon;
        this.current_slot = current_slot;
        this.blocked_days = blocked_days;
        this.preference_time = preference_time;
        this.fixed_blocks = fixed_blocks;
        this.tasks = tasks;
    }

    public int getHorizon() {
        return horizon;
    }

    public List<TaskDTO> getTasks() {
        return tasks;
    }

    public String getPreference_time() {
        return preference_time;
    }

    public List<Integer> getBlocked_days() {
        return blocked_days;
    }

    public int getCurrent_slot() {
        return current_slot;
    }

    public List<FreetimeDTO> getFixed_blocks() {
        return fixed_blocks;
    }

}
