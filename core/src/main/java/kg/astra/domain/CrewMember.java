package kg.astra.domain;

import java.util.Objects;
import java.util.Set;

/**
 * Crew member as the solver and risk model see them.
 *
 * @param remainingDoseBudgetMicroSv dose still allowed for this mission, µSv
 * @param status                     health status computed by Core
 */
public record CrewMember(String id, String name, Set<String> skills,
                         long remainingDoseBudgetMicroSv, HealthStatus status) {

    public CrewMember {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(status, "status");
        skills = skills == null ? Set.of() : Set.copyOf(skills);
    }

    public boolean canDo(Task task) {
        return task.requiredSkill() == null || skills.contains(task.requiredSkill());
    }
}
