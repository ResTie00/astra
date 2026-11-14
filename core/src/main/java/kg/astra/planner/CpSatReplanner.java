package kg.astra.planner;

import com.google.ortools.sat.CpSolverStatus;
import kg.astra.domain.MissionState;
import kg.astra.domain.PlanOption;
import kg.astra.domain.PlanStrategy;
import kg.astra.domain.ReplanResult;
import kg.astra.domain.Replanner;
import kg.astra.domain.Schedule;
import kg.astra.domain.SolverStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter between the contract (kg.astra.domain.Replanner) and the CP-SAT model (CpSatCore).
 * One CP-SAT run per strategy; the time limit from state.horizon() is split between the runs,
 * so the total time stays within the limit. Never throws on "no solution".
 */
public class CpSatReplanner implements Replanner {

    private static final List<PlanStrategy> STRATEGIES =
            List.of(PlanStrategy.BALANCED, PlanStrategy.MIN_DOSE, PlanStrategy.MIN_CHANGES);

    @Override
    public ReplanResult replan(MissionState state) {
        long startNanos = System.nanoTime();
        double perRunSeconds = state.horizon().solverTimeLimitSeconds() / STRATEGIES.size();

        List<PlanOption> options = new ArrayList<>();
        SolverStatus firstFailure = null;
        boolean allOptimal = true;

        for (PlanStrategy strategy : STRATEGIES) {
            CpSatCore.CoreResult r = CpSatCore.solve(state, strategy, perRunSeconds);

            if (!r.solved()) {
                // all strategies share the same hard rules: if they contradict, stop right away
                if (r.status() == CpSolverStatus.INFEASIBLE) {
                    return ReplanResult.noSolution(SolverStatus.INFEASIBLE, secondsSince(startNanos));
                }
                if (firstFailure == null) firstFailure = toSolverStatus(r.status());
                continue;
            }

            SolverStatus status = toSolverStatus(r.status());
            if (status != SolverStatus.OPTIMAL) allOptimal = false;

            Schedule schedule = new Schedule(r.assignments());
            options.add(new PlanOption(strategy, schedule, PlanDiff.diff(state, schedule),
                    status, r.objectiveValue()));
        }

        if (options.isEmpty()) {
            SolverStatus status = firstFailure == null ? SolverStatus.UNKNOWN : firstFailure;
            return ReplanResult.noSolution(status, secondsSince(startNanos));
        }
        return new ReplanResult(allOptimal ? SolverStatus.OPTIMAL : SolverStatus.FEASIBLE,
                options, secondsSince(startNanos));
    }

    private static SolverStatus toSolverStatus(CpSolverStatus s) {
        return switch (s) {
            case OPTIMAL -> SolverStatus.OPTIMAL;
            case FEASIBLE -> SolverStatus.FEASIBLE;
            case INFEASIBLE -> SolverStatus.INFEASIBLE;
            default -> SolverStatus.UNKNOWN;   // UNKNOWN, MODEL_INVALID
        };
    }

    private static double secondsSince(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000_000.0;
    }
}

