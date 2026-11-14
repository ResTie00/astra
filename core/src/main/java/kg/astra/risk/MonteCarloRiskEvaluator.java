package kg.astra.risk;

import kg.astra.domain.Assignment;
import kg.astra.domain.CrewMember;
import kg.astra.domain.CrewRisk;
import kg.astra.domain.DoseModel;
import kg.astra.domain.HealthStatus;
import kg.astra.domain.MissionState;
import kg.astra.domain.RiskEvaluator;
import kg.astra.domain.RiskReport;
import kg.astra.domain.Schedule;
import kg.astra.domain.SpeScenario;
import kg.astra.domain.Task;
import kg.astra.domain.TaskType;
import kg.astra.risk.MonteCarloEvaluator.AstronautSchedule;
import kg.astra.risk.MonteCarloEvaluator.RiskResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter between the contract (RiskEvaluator: Schedule + MissionState -> RiskReport)
 * and the calculation in MonteCarloEvaluator. A pure function: all randomness is in the scenarios.
 */
public class MonteCarloRiskEvaluator implements RiskEvaluator {

    private final MonteCarloEvaluator evaluator;

    public MonteCarloRiskEvaluator(DoseModel doseModel) {
        this.evaluator = new MonteCarloEvaluator(doseModel);
    }

    @Override
    public RiskReport evaluate(Schedule plan, MissionState state, List<SpeScenario> scenarios) {
        // 1) Schedule + tasks -> one timeline per crew member
        List<AstronautSchedule> timelines = timelines(plan, state);

        // 2) the calculation itself
        RiskResult r = evaluator.evaluate(timelines, scenarios);

        // 3) RiskResult -> RiskReport
        List<CrewRisk> byCrew = new ArrayList<>();
        for (CrewMember c : state.crew()) {
            byCrew.add(new CrewRisk(c.id(), r.doseP50MicroSv().get(c.id()),
                    r.doseP95MicroSv().get(c.id()), r.probExceed().get(c.id())));
        }
        return new RiskReport(byCrew, r.probAnyExceed(), r.fatiguedEvaHours(),
                riskScore(r), scenarios.size());
    }

    /**
     * Single number 0..100 for sorting options (higher = worse).
     * Simplest version: the probability that someone exceeds the budget, in percent.
     * It must give the same ordering as the formula in StubRiskEvaluator, so copy that formula here.
     */
    private static double riskScore(RiskResult r) {
        return 100.0 * r.probAnyExceed();
    }

    /**
     * Task type of every slot for every crew member. A slot with no assignment stays null,
     * and the evaluator counts it as INTERNAL (contract convention).
     * fatigued = status is not NORMAL. accumulated dose = 0: the limit is the REMAINING budget.
     */
    private static List<AstronautSchedule> timelines(Schedule plan, MissionState state) {
        int slots = state.horizon().slots();
        Map<String, Task> tasks = state.taskById();
        Map<String, TaskType[]> lines = new LinkedHashMap<>();
        for (CrewMember c : state.crew()) {
            lines.put(c.id(), new TaskType[slots]);
        }
        for (Assignment a : plan.assignments()) {
            Task task = tasks.get(a.taskId());
            TaskType[] line = lines.get(a.crewId());
            if (task == null || line == null) continue;   // unknown task or person: ignore
            int end = Math.min(slots, a.startSlot() + task.durationSlots());
            for (int s = Math.max(0, a.startSlot()); s < end; s++) {
                line[s] = task.type();
            }
        }

        List<AstronautSchedule> result = new ArrayList<>();
        for (CrewMember c : state.crew()) {
            result.add(new AstronautSchedule(c.id(), lines.get(c.id()),
                    c.status() != HealthStatus.NORMAL, 0L, c.remainingDoseBudgetMicroSv()));
        }
        return result;
    }
}
