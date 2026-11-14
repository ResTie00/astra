package kg.astra.domain;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Everything the solver and the risk model need. Assembled by Core from the database. */
public record MissionState(PlanningHorizon horizon, List<CrewMember> crew, List<Task> tasks,
                           Schedule currentSchedule, DoseTable doseTable, SpaceWeatherForecast forecast) {

    public MissionState {
        Objects.requireNonNull(horizon, "horizon");
        Objects.requireNonNull(doseTable, "doseTable");
        crew = List.copyOf(crew);
        tasks = List.copyOf(tasks);
        currentSchedule = currentSchedule == null ? Schedule.empty() : currentSchedule;
        forecast = forecast == null ? SpaceWeatherForecast.quiet() : forecast;
        if (doseTable.slots() != horizon.slots())
            throw new IllegalArgumentException("doseTable slots != horizon slots");
    }

    public Map<String, CrewMember> crewById() {
        return crew.stream().collect(Collectors.toMap(CrewMember::id, Function.identity()));
    }

    public Map<String, Task> taskById() {
        return tasks.stream().collect(Collectors.toMap(Task::id, Function.identity()));
    }
}
