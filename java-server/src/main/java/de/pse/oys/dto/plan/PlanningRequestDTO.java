package de.pse.oys.dto.plan;

import java.util.List;

public class PlanningRequestDTO {
    private int horizon;
    private int current_slot;
    private List<Integer> blocked_days;
    private  String preference_time;
    private List<FixedBlockDTO> fixed_blocks;
    private List<PlanningTaskDTO> tasks;

    public PlanningRequestDTO(int horizon, int current_slot, List<Integer> blocked_days, String preference_time,
                              List<FixedBlockDTO> fixed_blocks, List<PlanningTaskDTO> tasks) {
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

    public int getCurrent_slot() {
        return current_slot;
    }

    public List<Integer> getBlocked_days() {
        return blocked_days;
    }

    public String getPreference_time() {
        return preference_time;
    }

    public List<FixedBlockDTO> getFixed_blocks() {
        return fixed_blocks;
    }

    public List<PlanningTaskDTO> getTasks() {
        return tasks;
    }



}
