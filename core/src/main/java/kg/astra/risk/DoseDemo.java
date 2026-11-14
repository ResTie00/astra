package kg.astra.risk;

import kg.astra.domain.DoseModel;
import kg.astra.domain.ScenarioSampler;
import kg.astra.domain.SpaceWeatherForecast;
import kg.astra.domain.SpeScenario;
import kg.astra.domain.TaskType;

import java.util.List;

/**
 * Manual check of the dose model. Lives in src/test: it is not part of the application.
 * All numbers are DUMMY and do NOT match the simulator. Real values come from the team's
 * agreed source (configuration), never from this file.
 */
public class DoseDemo {
    public static void main(String[] args) {
        double nominalAttenuation = 0.2;
        DoseModel model = new SimpleDoseModel(new SimpleDoseModel.Params(
                2, 1, 1, 1,          // background per slot: EVA, INTERNAL, EXERCISE, SLEEP
                0.5,                 // storm dose per slot per pfu (unshielded)
                nominalAttenuation));

        // storm expected: onset slot 20, peak slot 24, lasts 10 slots, 100 pfu
        SpaceWeatherForecast forecast = new SpaceWeatherForecast(true, 20, 24, 10, 100.0);

        System.out.println("Expected EVA slot 24:   " + model.expectedDoseMicroSv(TaskType.EVA, 24, forecast));
        System.out.println("Expected SLEEP slot 24: " + model.expectedDoseMicroSv(TaskType.SLEEP, 24, forecast));
        System.out.println("Expected EVA slot 5:    " + model.expectedDoseMicroSv(TaskType.EVA, 5, forecast));

        ScenarioSampler sampler = new SpeScenarioSampler(nominalAttenuation,
                new SpeScenarioSampler.Params(0.5, 2.0, 0.05));
        List<SpeScenario> scenarios = sampler.sample(forecast, 3, 42L);   // fixed seed => repeatable
        for (SpeScenario s : scenarios) {
            System.out.println(s + " -> EVA slot 24 dose " + model.sampleDoseMicroSv(TaskType.EVA, 24, s));
        }
    }
}