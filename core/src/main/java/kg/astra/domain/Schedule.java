package kg.astra.domain;

import java.util.List;
import java.util.Optional;

//A full schedule: a list of assignments
public record Schedule(List<Assignment> assignments) {

    public Schedule {
        assignments = assignments == null ? List.of() : List.copyOf(assignments);
    }

    public static Schedule empty() {
        return new Schedule(List.of());
    }

    public Optional<Assignment> find(String taskId) {
        return assignments.stream().filter(a -> a.taskId().equals(taskId)).findFirst();
    }
}
