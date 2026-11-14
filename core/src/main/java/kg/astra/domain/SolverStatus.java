package kg.astra.domain;

/** Result status of a replanning run. */
public enum SolverStatus {
    /** Best possible plan, proven. */
    OPTIMAL,
    /** Valid plan, but optimality not proven within the time limit. */
    FEASIBLE,
    /** Constraints contradict each other: no valid plan exists. */
    INFEASIBLE,
    /** Nothing found within the time limit. */
    UNKNOWN,
    /** Produced by Core's greedy fallback, not by CP-SAT. */
    FALLBACK
}
