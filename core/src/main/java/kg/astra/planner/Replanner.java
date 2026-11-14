package kg.astra.planner;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import com.google.ortools.util.Domain;
import kg.astra.domain.DoseModel;
import kg.astra.domain.SpaceWeatherForecast;
import kg.astra.domain.TaskType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CP-SAT replanner, version 2.
 * Time = slots (1 slot = 30 min, UTC). Dose = whole microsieverts.
 * Rules: one executor per task; no overlaps per astronaut; status ACTION => no EVA;
 *        EVA never overlaps the storm window; dose of an astronaut stays within his budget.
 * DRAFT records below (Astronaut, Task, Assignment, Plan) must be agreed with Adilet.
 */
public class Replanner {

    public enum Status { NORMAL, ATTENTION, ACTION }

    /** remainingDoseBudgetMicroSv = limit minus already accumulated dose (computed by Core). */
    public record Astronaut(String id, Status status, long remainingDoseBudgetMicroSv) {}
    public record Task(String id, TaskType type, int durationSlots) {}
    public record Assignment(String taskId, String astronautId, int startSlot) {}
    public record Plan(List<Assignment> assignments, int makespanSlots) {}

    private final int horizonSlots;

    public Replanner(int horizonSlots) {
        this.horizonSlots = horizonSlots;
    }

    /** Returns a feasible plan, or empty if none exists within the time limit. */
    public Optional<Plan> solve(List<Astronaut> astronauts, List<Task> tasks,
                                SpaceWeatherForecast forecast, DoseModel doseModel) {
        Loader.loadNativeLibraries();
        CpModel model = new CpModel();

        int nA = astronauts.size();
        int nT = tasks.size();

        IntVar[] start = new IntVar[nT];
        BoolVar[][] assigned = new BoolVar[nT][nA];
        List<List<IntervalVar>> perAstronaut = new ArrayList<>();
        List<LinearExprBuilder> doseSum = new ArrayList<>();   // dose of each astronaut
        for (int a = 0; a < nA; a++) {
            perAstronaut.add(new ArrayList<>());
            doseSum.add(LinearExpr.newBuilder());
        }

        for (int t = 0; t < nT; t++) {
            Task task = tasks.get(t);
            int dur = task.durationSlots();
            int maxStart = horizonSlots - dur;
            if (maxStart < 0) return Optional.empty();           // task longer than the horizon

            // dose of this task for each possible start = sum of dose of its slots
            long[] doseByStart = new long[maxStart + 1];
            long maxDose = 0;
            for (int s = 0; s <= maxStart; s++) {
                long sum = 0;
                for (int slot = s; slot < s + dur; slot++) {
                    sum += doseModel.expectedDoseMicroSv(task.type(), slot, forecast);
                }
                doseByStart[s] = sum;
                maxDose = Math.max(maxDose, sum);
            }

            // start slot; for EVA during a storm, keep only starts that do not touch the storm window
            if (task.type() == TaskType.EVA && forecast.stormExpected()) {
                int stormEnd = forecast.onsetSlot() + forecast.durationSlots();
                List<Long> allowed = new ArrayList<>();
                for (int s = 0; s <= maxStart; s++) {
                    boolean beforeStorm = s + dur <= forecast.onsetSlot();
                    boolean afterStorm = s >= stormEnd;
                    if (beforeStorm || afterStorm) allowed.add((long) s);
                }
                if (allowed.isEmpty()) return Optional.empty();  // EVA cannot fit around the storm
                long[] values = allowed.stream().mapToLong(Long::longValue).toArray();
                start[t] = model.newIntVarFromDomain(Domain.fromValues(values), "start_" + task.id());
            } else {
                start[t] = model.newIntVar(0, maxStart, "start_" + task.id());
            }

            // taskDose = doseByStart[start]  (lookup table)
            IntVar taskDose = model.newIntVar(0, maxDose, "dose_" + task.id());
            model.addElement(start[t], doseByStart, taskDose);

            List<Literal> choices = new ArrayList<>();
            for (int a = 0; a < nA; a++) {
                Astronaut astro = astronauts.get(a);
                assigned[t][a] = model.newBoolVar("as_" + task.id() + "_" + astro.id());
                choices.add(assigned[t][a]);

                perAstronaut.get(a).add(model.newOptionalFixedSizeIntervalVar(
                        start[t], dur, assigned[t][a], "iv_" + task.id() + "_" + astro.id()));

                if (astro.status() == Status.ACTION && task.type() == TaskType.EVA) {
                    model.addEquality(assigned[t][a], 0);
                }

                // dose counted for this astronaut only if he does this task
                IntVar doseTA = model.newIntVar(0, maxDose, "dta_" + task.id() + "_" + astro.id());
                model.addEquality(doseTA, taskDose).onlyEnforceIf(assigned[t][a]);
                model.addEquality(doseTA, 0).onlyEnforceIf(assigned[t][a].not());
                doseSum.get(a).add(doseTA);
            }
            model.addExactlyOne(choices);
        }

        for (int a = 0; a < nA; a++) {
            model.addNoOverlap(perAstronaut.get(a));
            // total dose of the astronaut must not exceed his remaining budget
            model.addLessOrEqual(doseSum.get(a).build(), astronauts.get(a).remainingDoseBudgetMicroSv());
        }

        // placeholder objective: finish everything as early as possible
        IntVar makespan = model.newIntVar(0, horizonSlots, "makespan");
        for (int t = 0; t < nT; t++) {
            model.addLinearConstraint(
                    LinearExpr.newBuilder().add(makespan).addTerm(start[t], -1).build(),
                    tasks.get(t).durationSlots(), horizonSlots);
        }
        model.minimize(makespan);

        CpSolver solver = new CpSolver();
        solver.getParameters().setMaxTimeInSeconds(5.0);
        solver.getParameters().setNumWorkers(1);
        solver.getParameters().setRandomSeed(42);

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