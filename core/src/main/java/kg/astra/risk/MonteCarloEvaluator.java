package kg.astra.risk;

import kg.astra.domain.DoseModel;
import kg.astra.domain.PlanningHorizon;
import kg.astra.domain.SpeScenario;
import kg.astra.domain.TaskType;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Core of the Monte Carlo risk: for each random storm, add up the dose of every astronaut
 * and look at the distribution (median, 95th percentile, how often the limit is exceeded).
 * MonteCarloRiskEvaluator wraps this class into the contract (RiskEvaluator).
 *
 * The dose model must be thread-safe (immutable): scenarios are evaluated in parallel.
 */
public class MonteCarloEvaluator {

    /** Slot length in hours, taken from the contract (PlanningHorizon.SLOT_MINUTES). */
    public static final double SLOT_HOURS = PlanningHorizon.SLOT_MINUTES / 60.0;

    /**
     * Plan of ONE astronaut as the risk model sees it.
     * slots[i] = task type in slot i; null means "no assignment" = INTERNAL (contract convention).
     */
    public record AstronautSchedule(String astronautId, TaskType[] slots, boolean fatigued,
                                    long accumulatedDoseMicroSv, long doseLimitMicroSv) {}

    /**
     * @param probAnyExceed    share of storms where at least one astronaut exceeds the limit
     * @param probExceed       the same per astronaut
     * @param doseP50MicroSv   median total dose per astronaut (microSv)
     * @param doseP95MicroSv   95th percentile of the total dose per astronaut (microSv)
     * @param fatiguedEvaHours hours of EVA done by fatigued astronauts
     */
    public record RiskResult(double probAnyExceed, Map<String, Double> probExceed,
                             Map<String, Double> doseP50MicroSv, Map<String, Double> doseP95MicroSv,
                             double fatiguedEvaHours) {}

    private final DoseModel doseModel;

    public MonteCarloEvaluator(DoseModel doseModel) {
        this.doseModel = doseModel;
    }

    public RiskResult evaluate(List<AstronautSchedule> plan, List<SpeScenario> scenarios) {
        int n = scenarios.size();
        int m = plan.size();
        if (n == 0) throw new IllegalArgumentException("no scenarios");

        // 1) parallel part: every scenario fills its own row, so there are no shared writes
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

        // 2) sequential part: fixed order => the same result on every run
        int anyExceed = 0;
        int[] exceedCount = new int[m];
        for (int i = 0; i < n; i++) {
            boolean any = false;
            for (int a = 0; a < m; a++) {
                if (dose[i][a] > plan.get(a).doseLimitMicroSv()) {
                    exceedCount[a]++;
                    any = true;
                }
            }
            if (any) anyExceed++;
        }

        Map<String, Double> probExceed = new LinkedHashMap<>();
        Map<String, Double> p50 = new LinkedHashMap<>();
        Map<String, Double> p95 = new LinkedHashMap<>();
        for (int a = 0; a < m; a++) {
            double[] column = new double[n];             // doses of astronaut a over all storms
            for (int i = 0; i < n; i++) column[i] = dose[i][a];
            Arrays.sort(column);
            String id = plan.get(a).astronautId();
            probExceed.put(id, (double) exceedCount[a] / n);
            p50.put(id, percentile(column, 0.50));
            p95.put(id, percentile(column, 0.95));
        }

        // 3) EVA hours of fatigued people do not depend on the storm: count them from the plan
        double evaSlots = 0;
        for (AstronautSchedule s : plan) {
            if (!s.fatigued()) continue;
            for (TaskType type : s.slots()) {
                if (type == TaskType.EVA) evaSlots++;
            }
        }

        return new RiskResult((double) anyExceed / n, probExceed, p50, p95, evaSlots * SLOT_HOURS);
    }

    /** Nearest-rank percentile of an already sorted array. */
    private static double percentile(double[] sorted, double p) {
        int index = (int) Math.ceil(p * sorted.length) - 1;
        return sorted[Math.max(0, Math.min(sorted.length - 1, index))];
    }
}

