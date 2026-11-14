/**
 * Owner: Mansur. CP-SAT implementation of {@link kg.astra.domain.Replanner}.
 * Plain Java: import only from kg.astra.domain and com.google.ortools. No Spring annotations.
 * Plugged in via kg.astra.config.DomainConfig.
 */
package kg.astra.planner;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import kg.astra.domain.TaskType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Baseline replanner on CP-SAT.
 * Time is a discrete grid of slots (1 slot = 30 min, agreed value, UTC).
 * DRAFT: records below must be agreed with Adilet/Arsen in the first hour.
 */
public class Replanner {

    // ---- draft shared records (to be agreed with the team) ----
    public enum Status { NORMAL, ATTENTION, ACTION }

    public record Astronaut(String id, Status status) {}
    public record Task(String id, TaskType type, int durationSlots) {}
    public record Assignment(String taskId, String astronautId, int startSlot) {}
    public record Plan(List<Assignment> assignments, int makespanSlots) {}

    private final int horizonSlots;

    public Replanner(int horizonSlots) {
        this.horizonSlots = horizonSlots;
    }

    /** Returns a feasible plan, or empty if no plan exists within the time limit. */
    public Optional<Plan> solve(List<Astronaut> astronauts, List<Task> tasks) {
        Loader.loadNativeLibraries();                 // loads OR-Tools native code
        CpModel model = new CpModel();

        int nA = astronauts.size();
        int nT = tasks.size();

        IntVar[] start = new IntVar[nT];              // start slot of each task
        BoolVar[][] assigned = new BoolVar[nT][nA];   // assigned[t][a] = astronaut a does task t
        List<List<IntervalVar>> perAstronaut = new ArrayList<>();
        for (int a = 0; a < nA; a++) perAstronaut.add(new ArrayList<>());

        for (int t = 0; t < nT; t++) {
            Task task = tasks.get(t);
            start[t] = model.newIntVar(0, horizonSlots - task.durationSlots(), "start_" + task.id());

            List<Literal> choices = new ArrayList<>();
            for (int a = 0; a < nA; a++) {
                Astronaut astro = astronauts.get(a);
                assigned[t][a] = model.newBoolVar("as_" + task.id() + "_" + astro.id());
                choices.add(assigned[t][a]);

                // interval exists only if this astronaut was chosen for this task
                IntervalVar iv = model.newOptionalFixedSizeIntervalVar(
                        start[t], task.durationSlots(), assigned[t][a],
                        "iv_" + task.id() + "_" + astro.id());
                perAstronaut.get(a).add(iv);

                // status "ACTION" => no EVA
                if (astro.status() == Status.ACTION && task.type() == TaskType.EVA) {
                    model.addEquality(assigned[t][a], 0);
                }
            }
            model.addExactlyOne(choices);             // exactly one executor per task
        }

        // one astronaut cannot do two tasks at the same time
        for (int a = 0; a < nA; a++) {
            model.addNoOverlap(perAstronaut.get(a));
        }

        // placeholder objective: finish everything as early as possible
        IntVar makespan = model.newIntVar(0, horizonSlots, "makespan");
        for (int t = 0; t < nT; t++) {
            // makespan - start[t] >= duration  <=>  makespan >= start[t] + duration
            model.addLinearConstraint(
                    LinearExpr.newBuilder().add(makespan).addTerm(start[t], -1).build(),
                    tasks.get(t).durationSlots(), horizonSlots);
        }
        model.minimize(makespan);

        CpSolver solver = new CpSolver();
        solver.getParameters().setMaxTimeInSeconds(5.0);  // demo time limit
        solver.getParameters().setNumWorkers(1);          // single thread
        solver.getParameters().setRandomSeed(42);         // fixed seed => repeatable demo

        CpSolverStatus status = solver.solve(model);
        if (status != CpSolverStatus.OPTIMAL && status != CpSolverStatus.FEASIBLE) {
            return Optional.empty();
        }

        List<Assignment> result = new ArrayList<>();
        for (int t = 0; t < nT; t++) {
            for (int a = 0; a < nA; a++) {
                if (solver.booleanValue(assigned[t][a])) {
                    result.add(new Assignment(tasks.get(t).id(), astronauts.get(a).id(),
                            (int) solver.value(start[t])));
                }
            }
        }
        return Optional.of(new Plan(result, (int) solver.value(makespan)));
    }
}
