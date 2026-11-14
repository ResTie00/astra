package kg.astra.planner;

import com.google.ortools.Loader;
import kg.astra.domain.Assignment;
import kg.astra.domain.CrewMember;
import kg.astra.domain.DoseTable;
import kg.astra.domain.HealthStatus;
import kg.astra.domain.MissionState;
import kg.astra.domain.PlanningHorizon;
import kg.astra.domain.ReplanResult;
import kg.astra.domain.Schedule;
import kg.astra.domain.SpaceWeatherForecast;
import kg.astra.domain.Task;
import kg.astra.domain.TaskType;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Toy run on the real contract types. Dose numbers are FAKE, only to test the solver. */
public class CpSatReplannerDemo {
    public static void main(String[] args) {
        Loader.loadNativeLibraries();   // in the real app SolverConfig does this

        int slots = 24;                 // 24 one-hour slots
        // storm in slots [2, 10): a 4-slot EVA can only start at slot 10 or later
        SpaceWeatherForecast forecast = new SpaceWeatherForecast(true, 2, 5, 8, 100.0);

        // fake dose table: EVA 2 per slot, 10 inside the storm; everything else 1 per slot
        Map<TaskType, long[]> rows = new EnumMap<>(TaskType.class);
        for (TaskType type : TaskType.values()) {
            long[] row = new long[slots];
            Arrays.fill(row, type == TaskType.EVA ? 2 : 1);
            if (type == TaskType.EVA) {
                for (int s = 2; s < 10; s++) row[s] = 10;
            }
            rows.put(type, row);
        }
        DoseTable doseTable = new DoseTable(rows);

        List<CrewMember> crew = List.of(
                new CrewMember("A1", "Crew 1", Set.of("eva"), 1000, HealthStatus.ACT),    // no EVA
                new CrewMember("A2", "Crew 2", Set.of(), 1000, HealthStatus.WATCH),       // avoid loading
                new CrewMember("A3", "Crew 3", Set.of("eva"), 1000, HealthStatus.NORMAL));

        List<Task> tasks = List.of(
                new Task("eva-1", "EVA", TaskType.EVA, 4, "eva", true, 0, slots),
                new Task("exercise-1", "Exercise", TaskType.EXERCISE, 2, null, true, 0, slots),
                new Task("sleep-1", "Sleep", TaskType.SLEEP, 8, null, true, 0, slots),
                new Task("internal-1", "Lab work", TaskType.INTERNAL, 3, null, false, 0, slots));

        // current (bad) plan: the EVA is inside the storm and given to A1, who has status ACT
        Schedule current = new Schedule(List.of(
                new Assignment("eva-1", "A1", 4),
                new Assignment("exercise-1", "A2", 0),
                new Assignment("internal-1", "A2", 2),
                new Assignment("sleep-1", "A3", 12)));

        MissionState state = new MissionState(
                new PlanningHorizon(slots, 6.0, 42L), crew, tasks, current, doseTable, forecast);

        ReplanResult result = new CpSatReplanner().replan(state);
        System.out.println("status: " + result.status() + ", wall time: " + result.wallTimeSeconds());
        result.options().forEach(option -> {
            System.out.println("== " + option.strategy() + " (" + option.status()
                    + ", objective " + option.objectiveValue() + ")");
            option.schedule().assignments().forEach(a -> System.out.println("   " + a));
            option.changes().forEach(c -> System.out.println("   change: " + c));
        });
    }
}

