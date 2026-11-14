package kg.astra.risk;

import kg.astra.domain.DoseModel;
import kg.astra.domain.PlanningHorizon;
import kg.astra.domain.SpeScenario;
import kg.astra.domain.TaskType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Monte Carlo risk of a plan: for each random storm, add up the dose of every astronaut
 * and count how often the dose limit is exceeded.
 *
 * Usage (Core does this, planner and risk never call each other):
 *   scenarios = sampler.sampleAll(N, seed);          // ONCE
 *   for each candidate plan: evaluator.evaluate(plan, scenarios);   // same scenarios for all
 *
 * The dose model must be thread-safe (immutable), because scenarios are evaluated in parallel.
 */
public class MonteCarloEvaluator {

    /** Slot length in hours, taken from the contract (PlanningHorizon.SLOT_MINUTES = 60). */
    public static final double SLOT_HOURS = PlanningHorizon.SLOT_MINUTES / 60.0;

    /**
     * Plan of ONE astronaut as seen by the risk model (Core builds it from the planner's Plan).
     * slots[i] = task type in slot i; null means "no assignment" = INTERNAL.
     */
    public record AstronautSchedule(String astronautId, TaskType[] slots, boolean fatigued,
                                    long accumulatedDoseMicroSv, long doseLimitMicroSv) {}

    /**
     * @param probAnyExceed     share of storms where at least one astronaut exceeds the limit
     * @param probExceed        the same per astronaut
     * @param meanDoseMicroSv   mean total dose per astronaut (microSv)
     * @param fatiguedWorkHours hours of EVA + INTERNAL work done by fatigued astronauts
     * @param fatiguedEvaHours  hours of EVA done by fatigued astronauts
     */
    public record RiskResult(double probAnyExceed, Map<String, Double> probExceed,
                             Map<String, Double> meanDoseMicroSv,
                             double fatiguedWorkHours, double fatiguedEvaHours) {}

    private final DoseModel doseModel;

    public MonteCarloEvaluator(DoseModel doseModel) {
        this.doseModel = doseModel;
    }

    public RiskResult evaluate(List<AstronautSchedule> plan, List<SpeScenario> scenarios) {
        int n = scenarios.size();
        int m = plan.size();

        // 1) parallel part: every scenario fills its own row, so no shared writes
        double[][] dose = new double[n][m];
        IntStream.range(0, n).parallel().forEach(i -> {
            SpeScenario scenario = scenarios.get(i);
            for (int a = 0; a < m; a++) {
                AstronautSchedule s = plan.get(a);
                double total = s.accumulatedDoseMicroSv();
                for (int slot = 0; slot < s.slots().length; slot++) {
                    TaskType type = s.slots()[slot] == null ? TaskType.INTERNAL : s.slots()[slot];
                    total += doseModel.sampleDoseMicroSv(type, slot, scenario);
                }
                dose[i][a] = total;
            }
        });

        // 2) sequential part: fixed summation order => same result on every run
        Map<String, Double> probExceed = new LinkedHashMap<>();
        Map<String, Double> meanDose = new LinkedHashMap<>();
        int[] exceedCount = new int[m];
        double[] doseSum = new double[m];
        int anyExceed = 0;
        for (int i = 0; i < n; i++) {
            boolean any = false;
            for (int a = 0; a < m; a++) {
                doseSum[a] += dose[i][a];
                if (dose[i][a] > plan.get(a).doseLimitMicroSv()) {
                    exceedCount[a]++;
                    any = true;
                }
            }
            if (any) anyExceed++;
        }
        for (int a = 0; a < m; a++) {
            String id = plan.get(a).astronautId();
            probExceed.put(id, (double) exceedCount[a] / n);
            meanDose.put(id, doseSum[a] / n);
        }

        // 3) fatigue hours do not depend on the storm: count them straight from the plan
        double workSlots = 0;
        double evaSlots = 0;
        for (AstronautSchedule s : plan) {
            if (!s.fatigued()) continue;
            for (TaskType type : s.slots()) {
                if (type == TaskType.EVA) {
                    evaSlots++;
                    workSlots++;
                } else if (type == null || type == TaskType.INTERNAL) {
                    workSlots++;
                }
            }
        }

        return new RiskResult((double) anyExceed / n, probExceed, meanDose,
                workSlots * SLOT_HOURS, evaSlots * SLOT_HOURS);
    }
}
