package kg.astra.planner;

import kg.astra.crew.Crew;
import kg.astra.planner.Replanner.*;

import java.util.List;

/** Toy run: 3 astronauts, one of them in ACTION status, 3 tasks. */
public class ReplannerDemo {
    public static void main(String[] args) {
        List<Astronaut> crew = List.of(
                new Astronaut("A1", Status.ACTION),   // must NOT get the EVA
                new Astronaut("A2", Status.NORMAL),
                new Astronaut("A3", Status.ATTENTION));

        List<Replanner.Task> tasks = List.of(
                new Task("eva-1", Replanner.TaskType.EVA, 8),        // 4 hours
                new Task("train-1", Replanner.TaskType.TRAINING, 4), // 2 hours
                new Task("work-1", Replanner.TaskType.WORK, 6));     // 3 hours

        Replanner replanner = new Replanner(48);           // 48 slots = 24 hours
        replanner.solve(crew, tasks).ifPresentOrElse(
                plan -> {
                    System.out.println("Makespan (slots): " + plan.makespanSlots());
                    plan.assignments().forEach(System.out::println);
                },
                () -> System.out.println("No plan found"));
    }
}
