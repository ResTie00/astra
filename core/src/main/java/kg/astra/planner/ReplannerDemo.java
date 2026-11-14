package kg.astra.planner;

import kg.astra.domain.Task;
import kg.astra.domain.TaskType;
import kg.astra.planner.Replanner.*;
import java.util.List;

/** Toy run: 3 astronauts, one of them in ACTION status, 3 tasks. */
public class ReplannerDemo {
    public static void main(String[] args) {
        List<Astronaut> crew = List.of(
                new Astronaut("A1", Status.ACTION),   // must NOT get the EVA
                new Astronaut("A2", Status.NORMAL),
                new Astronaut("A3", Status.ATTENTION));

        List<Task> tasks = List.of(
                new Task(
                        "T1",
                        "Assemble Engine",
                        TaskType.EVA,
                        3,
                        "MECHANIC",
                        true,
                        0,
                        8
                ),
                new Task(
                        "T2",
                        "Paint Body",
                        TaskType.EXERCISE,
                        2,
                        "PAINTER",
                        true,
                        2,
                        10
                ),
                new Task(
                        "T3",
                        "Quality Inspection",
                        TaskType.INTERNAL,
                        1,
                        "INSPECTOR",
                        false,
                        5,
                        12
                )
        );     // 3 hours

        Replanner replanner = new Replanner(48);           // 48 slots = 24 hours
        replanner.solve(crew, tasks).ifPresentOrElse(
                plan -> {
                    System.out.println("Makespan (slots): " + plan.makespanSlots());
                    plan.assignments().forEach(System.out::println);
                },
                () -> System.out.println("No plan found"));
    }
}