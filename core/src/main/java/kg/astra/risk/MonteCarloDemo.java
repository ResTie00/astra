package kg.astra.risk;

import kg.astra.domain.SpaceWeatherForecast;
import kg.astra.domain.SpeScenario;
import kg.astra.domain.TaskType;
import kg.astra.risk.MonteCarloEvaluator.AstronautSchedule;

import java.util.Arrays;
import java.util.List;

/** Old plan vs new plan on the SAME storms. All numbers are DUMMY, not real data. */
public class MonteCarloDemo {

    static final int HORIZON = 48;

    public static void main(String[] args) {
        double attenuation = 0.2;
        SimpleDoseModel model = new SimpleDoseModel(new SimpleDoseModel.Params(
                2, 1, 1, 1, 0.5, attenuation));

        // storm in slots [4, 14), 100 pfu (dummy)
        SpaceWeatherForecast forecast = new SpaceWeatherForecast(true, 4, 8, 10, 100.0);
        SpeScenarioSampler sampler = new SpeScenarioSampler(forecast, attenuation,
                new SpeScenarioSampler.Params(0.5, 2.0, 0.05));

        // ONE list of scenarios for both plans => fair comparison; fixed seed => repeatable
        List<SpeScenario> scenarios = sampler.sampleAll(10_000, 42L);

        long limit = 300; // dummy limit
        // old plan: tired A1 does the EVA inside the storm window (slots 6..13)
        List<AstronautSchedule> oldPlan = List.of(
                schedule("A1", true, 6, limit),
                schedule("A3", false, -1, limit));
        // new plan: rested A3 does the EVA after the storm (slots 14..21)
        List<AstronautSchedule> newPlan = List.of(
                schedule("A1", true, -1, limit),
                schedule("A3", false, 14, limit));

        MonteCarloEvaluator evaluator = new MonteCarloEvaluator(model);
        print("OLD plan", evaluator.evaluate(oldPlan, scenarios));
        print("NEW plan", evaluator.evaluate(newPlan, scenarios));
    }

    /** evaStart < 0 means no EVA; every other slot is INTERNAL work. */
    static AstronautSchedule schedule(String id, boolean fatigued, int evaStart, long limit) {
        TaskType[] slots = new TaskType[HORIZON];
        Arrays.fill(slots, TaskType.INTERNAL);
        if (evaStart >= 0) {
            for (int s = evaStart; s < evaStart + 8; s++) slots[s] = TaskType.EVA;
        }
        return new AstronautSchedule(id, slots, fatigued, 0, limit);
    }

    static void print(String name, MonteCarloEvaluator.RiskResult r) {
        System.out.println(name);
        System.out.println("  P(any exceeds limit): " + r.probAnyExceed());
        System.out.println("  P(exceed) per astronaut: " + r.probExceed());
        System.out.println("  mean dose (microSv): " + r.meanDoseMicroSv());
        System.out.println("  fatigued work hours: " + r.fatiguedWorkHours()
                + ", fatigued EVA hours: " + r.fatiguedEvaHours());
    }
}