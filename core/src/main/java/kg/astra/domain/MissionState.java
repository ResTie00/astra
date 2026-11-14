package kg.astra.domain;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

// everything the solver and the risk model need, in one object.
// Core builds it from the database. the solver never touches the database itself
public record MissionState(PlanningHorizon horizon, List<CrewMember> crew, List<Task> tasks,
                           Schedule currentSchedule, DoseTable doseTable, SpaceWeatherForecast forecast) {

    public MissionState {
        Objects.requireNonNull(horizon, "horizon");
        Objects.requireNonNull(doseTable, "doseTable");
        // copies, so the state can't change while the solver is working with it
        crew = List.copyOf(crew);
        tasks = List.copyOf(tasks);
        // no current plan or no storm are normal cases, so use empty defaults instead of null
        currentSchedule = currentSchedule == null ? Schedule.empty() : currentSchedule;
        forecast = forecast == null ? SpaceWeatherForecast.quiet() : forecast;
        // catch a mismatch here, not as an index error deep inside the solver
        if (doseTable.slots() != horizon.slots())
            throw new IllegalArgumentException("doseTable slots != horizon slots");
    }

    // quick lookup by id, used when we turn ids back into names and tasks
    public Map<String, CrewMember> crewById() {
        return crew.stream().collect(Collectors.toMap(CrewMember::id, Function.identity()));
    }

    public Map<String, Task> taskById() {
        return tasks.stream().collect(Collectors.toMap(Task::id, Function.identity()));
    }
}