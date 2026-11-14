package kg.astra.domain;

import java.util.List;


// Output of the replanner. If no plan was found, options is empty and status says why
// The replanner never throws for "no solution": Core then switches to the greedy fallback
public record ReplanResult(SolverStatus status, List<PlanOption> options, double wallTimeSeconds) {

    public ReplanResult {
        options = options == null ? List.of() : List.copyOf(options);
    }

    public static ReplanResult noSolution(SolverStatus status, double wallTimeSeconds) {
        return new ReplanResult(status, List.of(), wallTimeSeconds);
    }

    public boolean hasOptions() {
        return !options.isEmpty();
    }
}
