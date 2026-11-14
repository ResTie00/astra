package kg.astra.domain;

import java.util.List;

// One candidate schedule produced by the replanner.
public record PlanOption(PlanStrategy strategy, Schedule schedule, List<PlanChange> changes,
                         SolverStatus status, long objectiveValue) {

    public PlanOption {
        changes = changes == null ? List.of() : List.copyOf(changes);
    }
}
