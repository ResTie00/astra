package kg.astra.domain.stub;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Straightforward Monte Carlo, sequential. Good enough to run end to end; Arsen owns the real one. */
public class StubRiskEvaluator implements RiskEvaluator {

    private final DoseModel doseModel;

    public StubRiskEvaluator(DoseModel doseModel) {
        this.doseModel = doseModel;
    }

    @Override
    public RiskReport evaluate(Schedule plan, MissionState state, List<SpeScenario> scenarios) {
        int slots = state.horizon().slots();
        Map<String, Task> tasks = state.taskById();
        int runs = Math.max(1, scenarios.size());

        List<CrewRisk> byCrew = new ArrayList<>();
        boolean[] anyExceeded = new boolean[runs];
        double impairedEvaHours = 0;

        for (CrewMember member : state.crew()) {
            TaskType[] timeline = timelineOf(member.id(), plan, tasks, slots);

            if (member.status() != HealthStatus.NORMAL) {
                for (TaskType t : timeline) if (t == TaskType.EVA) impairedEvaHours++;
            }

            double[] doses = new double[runs];
            int exceeded = 0;
            for (int r = 0; r < scenarios.size(); r++) {
                double sum = 0;
                for (int s = 0; s < slots; s++) sum += doseModel.sampleDoseMicroSv(timeline[s], s, scenarios.get(r));
                doses[r] = sum;
                if (sum > member.remainingDoseBudgetMicroSv()) {
                    exceeded++;
                    anyExceeded[r] = true;
                }
            }
            Arrays.sort(doses);
            byCrew.add(new CrewRisk(member.id(), percentile(doses, 0.5), percentile(doses, 0.95),
                    (double) exceeded / runs));
        }

        int crewExceeded = 0;
        for (boolean b : anyExceeded) if (b) crewExceeded++;
        double crewExceedProbability = (double) crewExceeded / runs;
        double riskScore = Math.min(100, crewExceedProbability * 100 + impairedEvaHours * 5);
        return new RiskReport(byCrew, crewExceedProbability, impairedEvaHours, riskScore, scenarios.size());
    }

    /** What the person is doing in each slot; free slots count as INTERNAL. */
    private static TaskType[] timelineOf(String crewId, Schedule plan, Map<String, Task> tasks, int slots) {
        TaskType[] timeline = new TaskType[slots];
        Arrays.fill(timeline, TaskType.INTERNAL);
        for (Assignment a : plan.assignments()) {
            if (!a.crewId().equals(crewId)) continue;
            Task task = tasks.get(a.taskId());
            if (task == null) continue;
            for (int s = a.startSlot(); s < a.startSlot() + task.durationSlots() && s < slots; s++) timeline[s] = task.type();
        }
        return timeline;
    }

    private static double percentile(double[] sorted, double p) {
        if (sorted.length == 0) return 0;
        int idx = (int) Math.ceil(p * sorted.length) - 1;
        return sorted[Math.clamp(idx, 0, sorted.length - 1)];
    }
}
