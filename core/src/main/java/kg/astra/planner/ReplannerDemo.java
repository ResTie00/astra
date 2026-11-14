package kg.astra.planner;

import kg.astra.domain.DoseModel;
import kg.astra.domain.SpaceWeatherForecast;
import kg.astra.domain.SpeScenario;
import kg.astra.domain.TaskType;
import kg.astra.planner.Replanner.*;

import java.util.List;

/** Toy run. The dose model here is FAKE (only for testing the solver). Real one is Arsen's. */
public class ReplannerDemo {
    public static void main(String[] args) {
        DoseModel fakeDose = new DoseModel() {
            @Override
            public long expectedDoseMicroSv(TaskType type, int slot, SpaceWeatherForecast f) {
                if (type == TaskType.EVA) return f.covers(slot) ? 10 : 2;
                return 1;
            }
            @Override
            public double sampleDoseMicroSv(TaskType type, int slot, SpeScenario s) {
                return 0; // not used by the solver
            }
        };

        // storm in slots [4, 14): the EVA (8 slots) cannot start before slot 14
        SpaceWeatherForecast forecast = new SpaceWeatherForecast(true, 4, 8, 10, 100.0);

        List<Astronaut> crew = List.of(
                new Astronaut("A1", Status.ACTION, 1000),   // no EVA allowed
                new Astronaut("A2", Status.NORMAL, 5),      // tiny budget: cannot take EVA
                new Astronaut("A3", Status.NORMAL, 1000));

        List<Task> tasks = List.of(
                new Task("eva-1", TaskType.EVA, 8),
                new Task("exercise-1", TaskType.EXERCISE, 4),
                new Task("sleep-1", TaskType.SLEEP, 16),
                new Task("internal-1", TaskType.INTERNAL, 6));

        new Replanner(48).solve(crew, tasks, forecast, fakeDose).ifPresentOrElse(
                plan -> {
                    System.out.println("Makespan (slots): " + plan.makespanSlots());
                    plan.assignments().forEach(System.out::println);
                },
                () -> System.out.println("No plan found"));
    }
}
