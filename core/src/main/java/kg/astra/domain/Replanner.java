package kg.astra.domain;

/**
 * Owner: Mansur (package kg.astra.planner, plain Java, no Spring).
 * Produces 2-3 candidate schedules (one per PlanStrategy).
 * Must respect: one executor per task, no overlap per person, dose budget,
 * ACT status = no EVA, task time windows. Must not throw on "no solution".
 */
public interface Replanner {
    ReplanResult replan(MissionState state);
}
