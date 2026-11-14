package kg.astra.planner;

import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;
import com.google.ortools.sat.Literal;
import com.google.ortools.util.Domain;
import kg.astra.domain.Assignment;
import kg.astra.domain.CrewMember;
import kg.astra.domain.DoseTable;
import kg.astra.domain.HealthStatus;
import kg.astra.domain.MissionState;
import kg.astra.domain.PlanStrategy;
import kg.astra.domain.SpaceWeatherForecast;
import kg.astra.domain.Task;
import kg.astra.domain.TaskType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CP-SAT model on the contract types. Time = integer slots (PlanningHorizon.SLOT_MINUTES).

 * Hard rules (the same for every strategy):
 *   one executor per task; no overlap per person; task time window; skills (CrewMember.canDo);
 *   status ACT => no EVA; EVA never touches the storm window;
 *   dose of a person stays within his remaining budget (doses come from state.doseTable()).

 * Objective = weighted sum of four terms; the strategy only changes the weights:
 *   changes  = number of tasks whose executor or start differs from state.currentSchedule()
 *   dose     = total dose of all tasks (depends on the start slot, from the DoseTable)
 *   makespan = the latest end of all tasks
 *   watch    = slots of work given to a WATCH person

 * Does NOT load native libraries (SolverConfig does it) and does NOT throw on "no solution".
 */
public final class CpSatCore {

    /** Weights are tunable constants: check them on real data. */
    private record Weights(long changes, long dose, long makespan, long watch) {}

    private static Weights weights(PlanStrategy strategy) {
        return switch (strategy) {
            case MIN_CHANGES -> new Weights(100, 0, 1, 10);
            case MIN_DOSE -> new Weights(0, 100, 1, 10);
            case BALANCED -> new Weights(10, 1, 1, 10);
        };
    }

    /** Internal result. CpSatReplanner turns it into the contract's types. */
    public record CoreResult(CpSolverStatus status, List<Assignment> assignments,
                             long objectiveValue, double wallTimeSeconds) {
        public boolean solved() {
            return status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE;
        }
    }

    public CpSatCore() {}

    public static CoreResult solve(MissionState state, PlanStrategy strategy, double timeLimitSeconds) {
        final Weights w = weights(strategy);
        final int horizon = state.horizon().slots();
        final List<CrewMember> crew = state.crew();
        final List<Task> tasks = state.tasks();
        final DoseTable doseTable = state.doseTable();
        final SpaceWeatherForecast forecast = state.forecast();

        final int nA = crew.size();
        final int nT = tasks.size();
        Map<String, Integer> crewIndex = new HashMap<>();
        for (int a = 0; a < nA; a++) crewIndex.put(crew.get(a).id(), a);

        CpModel model = new CpModel();
        IntVar[] start = new IntVar[nT];
        BoolVar[][] assigned = new BoolVar[nT][nA];
        List<List<IntervalVar>> perPerson = new ArrayList<>();
        List<LinearExprBuilder> doseSum = new ArrayList<>();
        for (int a = 0; a < nA; a++) {
            perPerson.add(new ArrayList<>());
            doseSum.add(LinearExpr.newBuilder());
        }
        LinearExprBuilder objective = LinearExpr.newBuilder();

        for (int t = 0; t < nT; t++) {
            Task task = tasks.get(t);
            int dur = task.durationSlots();

            // time window: start in [earliestStart, min(latestEnd, horizon) - dur]
            int lo = task.earliestStartSlot();
            int hi = Math.min(task.latestEndSlot(), horizon) - dur;

            // allowed starts; an EVA must not overlap the storm window [onset, onset + duration)
            boolean avoidStorm = task.type() == TaskType.EVA && forecast.stormExpected();
            int stormStart = forecast.onsetSlot();
            int stormEnd = forecast.onsetSlot() + forecast.durationSlots();
            List<Long> allowed = new ArrayList<>();
            for (int s = lo; s <= hi; s++) {
                boolean touchesStorm = s < stormEnd && s + dur > stormStart;
                if (avoidStorm && touchesStorm) continue;
                allowed.add((long) s);
            }
            if (allowed.isEmpty()) {
                return new CoreResult(CpSolverStatus.INFEASIBLE, List.of(), 0, 0.0);
            }
            long[] values = allowed.stream().mapToLong(Long::longValue).toArray();
            start[t] = model.newIntVarFromDomain(Domain.fromValues(values), "start_" + task.id());

            // dose of this task for each possible start slot (lookup table from Core's DoseTable)
            long[] doseByStart = new long[hi + 1];
            long maxDose = 0;
            for (int s = 0; s <= hi; s++) {
                doseByStart[s] = doseTable.forTask(task, s);
                maxDose = Math.max(maxDose, doseByStart[s]);
            }
            IntVar taskDose = model.newIntVar(0, maxDose, "dose_" + task.id());
            model.addElement(start[t], doseByStart, taskDose);
            if (w.dose() > 0) objective.addTerm(taskDose, w.dose());

            List<Literal> choices = new ArrayList<>();
            for (int a = 0; a < nA; a++) {
                CrewMember person = crew.get(a);
                assigned[t][a] = model.newBoolVar("as_" + task.id() + "_" + person.id());
                choices.add(assigned[t][a]);

                perPerson.get(a).add(model.newOptionalFixedSizeIntervalVar(
                        start[t], dur, assigned[t][a], "iv_" + task.id() + "_" + person.id()));

                // skills and health status: forbidden pairs are fixed to 0
                boolean forbidden = !person.canDo(task)
                        || (person.status() == HealthStatus.ACT && task.type() == TaskType.EVA);
                if (forbidden) {
                    model.addEquality(assigned[t][a], 0);
                }

                // soft penalty: work given to a WATCH person
                if (person.status() == HealthStatus.WATCH) {
                    objective.addTerm(assigned[t][a], w.watch() * dur);
                }

                // dose is counted for this person only if he does this task
                IntVar doseTA = model.newIntVar(0, maxDose, "dta_" + task.id() + "_" + person.id());
                model.addEquality(doseTA, taskDose).onlyEnforceIf(assigned[t][a]);
                model.addEquality(doseTA, 0).onlyEnforceIf(assigned[t][a].not());
                doseSum.get(a).add(doseTA);
            }
            model.addExactlyOne(choices);   // exactly one executor

            // "changed" = the task has another executor OR another start than in the current schedule
            if (w.changes() > 0) {
                Optional<Assignment> old = state.currentSchedule().find(task.id());
                Integer oldIdx = old.map(x -> crewIndex.get(x.crewId())).orElse(null);
                if (old.isPresent() && oldIdx != null) {
                    int oldStart = old.get().startSlot();
                    BoolVar sameStart = model.newBoolVar("same_start_" + task.id());
                    model.addEquality(start[t], oldStart).onlyEnforceIf(sameStart);
                    model.addDifferent(start[t], oldStart).onlyEnforceIf(sameStart.not());

                    BoolVar changed = model.newBoolVar("changed_" + task.id());
                    // changed => (not same executor) or (not same start)
                    model.addBoolOr(new Literal[] {assigned[t][oldIdx].not(), sameStart.not()})
                            .onlyEnforceIf(changed);
                    // not changed => same executor and same start
                    model.addBoolAnd(new Literal[] {assigned[t][oldIdx], sameStart})
                            .onlyEnforceIf(changed.not());
                    objective.addTerm(changed, w.changes());
                }
            }
        }

        for (int a = 0; a < nA; a++) {
            model.addNoOverlap(perPerson.get(a));   // one person cannot do two tasks at once
            model.addLessOrEqual(doseSum.get(a).build(), crew.get(a).remainingDoseBudgetMicroSv());
        }

        // makespan = the latest end of all tasks
        IntVar makespan = model.newIntVar(0, horizon, "makespan");
        for (int t = 0; t < nT; t++) {
            model.addLinearConstraint(
                    LinearExpr.newBuilder().add(makespan).addTerm(start[t], -1).build(),
                    tasks.get(t).durationSlots(), horizon);
        }
        objective.addTerm(makespan, w.makespan());
        model.minimize(objective.build());

        CpSolver solver = new CpSolver();
        solver.getParameters().setMaxTimeInSeconds(timeLimitSeconds);
        solver.getParameters().setNumWorkers(1);
        solver.getParameters().setRandomSeed((int) state.horizon().seed());

        CpSolverStatus status = solver.solve(model);
        double wall = solver.wallTime();
        if (status != CpSolverStatus.OPTIMAL && status != CpSolverStatus.FEASIBLE) {
            return new CoreResult(status, List.of(), 0, wall);
        }

        List<Assignment> result = new ArrayList<>();
        for (int t = 0; t < nT; t++) {
            for (int a = 0; a < nA; a++) {
                if (solver.booleanValue(assigned[t][a])) {
                    result.add(new Assignment(tasks.get(t).id(), crew.get(a).id(),
                            (int) solver.value(start[t])));
                }
            }
        }
        return new CoreResult(status, result, Math.round(solver.objectiveValue()), wall);
    }
}

